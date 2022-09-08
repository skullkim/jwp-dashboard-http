package nextstep.jwp.view;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.function.Function;
import nextstep.jwp.db.InMemoryUserRepository;
import nextstep.jwp.model.User;
import org.apache.coyote.common.Charset;
import org.apache.coyote.common.HttpVersion;
import org.apache.coyote.common.MediaType;
import org.apache.coyote.common.request.Request;
import org.apache.coyote.common.response.Response;
import org.apache.coyote.common.response.Status;
import org.utils.ResourceGenerator;

public class LoginHandler implements Function<Request, Response> {

    @Override
    public Response apply(final Request request) {
        final String password = request.getBodyValue("password")
                .orElseThrow(() -> new IllegalArgumentException(Request.UNKNOWN_QUERY));
        final Optional<User> user = findUser(request);

        String locationUrl = "/index";
        String responseBody = "";
        Status statusCode = Status.FOUND;
        if (user.isEmpty() || !user.get().checkPassword(password)) {
            locationUrl = "/401";
            responseBody = ResourceGenerator.getStaticResource("/401");
            statusCode = Status.UNAUTHORIZED;
        }

        return Response.builder(HttpVersion.HTTP11, statusCode)
                .setContentType(MediaType.TEXT_HTML, Charset.UTF8)
                .setContentLength(responseBody.getBytes(StandardCharsets.UTF_8).length)
                .setLocation(locationUrl)
                .setBody(responseBody)
                .build();
    }

    private Optional<User> findUser(final Request request) {
        final String userAccount = request.getBodyValue("account")
                .orElseThrow(() -> new IllegalArgumentException(Request.UNKNOWN_QUERY));
        return InMemoryUserRepository.findByAccount(userAccount);
    }
}
