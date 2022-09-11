package org.apache.coyote.common.request;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.coyote.common.HttpVersion;
import org.apache.coyote.common.MediaType;
import org.apache.coyote.common.header.Cookie;
import org.apache.coyote.common.header.Header;
import org.apache.coyote.common.request.parser.UriParser;
import org.apache.coyote.common.request.parser.bodyparser.BodyParserMapper;

public class Request {

    public static final String UNKNOWN_QUERY = "Could not find query string";

    private static final int START_LINE = 0;
    private static final int HEADER_START = 1;
    private static final int METHOD = 0;
    private static final int URL = 1;
    private static final int HTTP_VERSION = 2;
    private static final String HEADER_BODY_DELIMITER = "";
    private static final String HEADER_KEY_VALUE_DELIMITER = ": ";

    private final HttpVersion httpVersion;
    private final RequestMethod method;
    private final String path;
    private final Map<String, String> queryString;
    private final Map<String, String> headers;
    private final Map<String, String> body;

    private final Cookie cookie;


    public Request(final String rawRequest) {
        final String[] parsedRequest = rawRequest.split("\r\n");
        final String[] requestStartLine = parsedRequest[START_LINE].split(" ");

        final URI uri = URI.create(requestStartLine[URL]);
        this.method = RequestMethod.of(requestStartLine[METHOD]);
        this.httpVersion = HttpVersion.of(requestStartLine[HTTP_VERSION]);
        this.path = UriParser.getPath(uri);
        this.queryString = UriParser.getQueryString(uri);

        final int headerBodyDelimiterIndex = getHeaderBodyDelimiterIndex(parsedRequest);
        this.headers = parseHeaders(parsedRequest, headerBodyDelimiterIndex);
        this.cookie = parseCookie(headers);
        this.body = parseBody(parsedRequest, headerBodyDelimiterIndex,
                MediaType.of(headers.get(Header.CONTENT_TYPE.getValue())));
    }

    private int getHeaderBodyDelimiterIndex(final String[] parsedRequest) {
        final int index = Arrays.asList(parsedRequest)
                .indexOf(HEADER_BODY_DELIMITER);
        if (index == -1) {

            return parsedRequest.length;

        }
        return index;
    }

    private Map<String, String> parseHeaders(final String[] request, final int headerBodyDelimiterIndex) {
        return Arrays.asList(request)
                .subList(HEADER_START, headerBodyDelimiterIndex)
                .stream()
                .map(header -> header.split(HEADER_KEY_VALUE_DELIMITER))
                .collect(Collectors.toMap(parsedHeader -> parsedHeader[0], parseHeader -> parseHeader[1]));
    }

    private Cookie parseCookie(final Map<String, String> headers) {
        final String rawCookie = headers.getOrDefault(Header.COOKIE.getValue(), "");
        return new Cookie(rawCookie);
    }

    private Map<String, String> parseBody(final String[] parsedRequest,
                                          final int headerBodyDelimiterIndex,
                                          final MediaType mediaType) {
        if (headerBodyDelimiterIndex <= 0 || parsedRequest.length <= headerBodyDelimiterIndex) {
            return new HashMap<>();
        }
        final String rawBody = String.join("", Arrays.asList(parsedRequest)
                .subList(headerBodyDelimiterIndex + 1, parsedRequest.length));
        return BodyParserMapper.of(mediaType)
                .apply(rawBody);
    }

    public boolean isGetRequest() {
        return this.method.equals(RequestMethod.GET);
    }

    public String getPath() {
        return path;
    }

    public Optional<String> getQueryStringValue(final String key) {
        return Optional.ofNullable(queryString.get(key));
    }

    public Optional<String> getBodyValue(final String key) {
        return Optional.ofNullable(body.get(key));
    }

    public Cookie getCookie() {
        return cookie;
    }

    public Optional<String> getCookie(final String key) {
        return cookie.getValue(key);
    }
}
