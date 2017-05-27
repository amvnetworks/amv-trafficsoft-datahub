package org.amv.trafficsoft.restclient.demo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/hello")
public class HelloController {

    @GetMapping
    public String index() {
        return "{" +
                "\"message\": \"Hello World.\"" +
                "}";
    }
}
