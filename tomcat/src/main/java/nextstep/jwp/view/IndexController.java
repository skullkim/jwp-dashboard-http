package nextstep.jwp.view;

import java.nio.charset.StandardCharsets;
import org.apache.coyote.common.controller.AbstractController;
import org.apache.coyote.common.request.Request;
import org.apache.coyote.common.response.Response;
import org.utils.ResourceGenerator;

public class IndexController extends AbstractController {

    @Override
    protected void doGet(final Request request, final Response response) throws Exception {
        final String responseBody = ResourceGenerator.getStaticResource(Url.ROOT.getValue());

        response.setContentLength(responseBody.getBytes(StandardCharsets.UTF_8).length)
                .setBody(responseBody);
    }
}
