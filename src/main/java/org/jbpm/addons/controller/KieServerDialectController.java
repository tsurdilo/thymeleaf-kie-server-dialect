package org.jbpm.addons.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class KieServerDialectController {

    @RequestMapping("/ksdstartprocess/{pid}/{cid}")
    public String startProcessInstance(@PathVariable("pid") String processid,
                                       @PathVariable("cid") String containerid,
                                       Model model) {
        model.addAttribute("containerid",
                           containerid);
        model.addAttribute("processid",
                           processid);
        model.addAttribute("formlocation",
                           "/rest/server/containers/" + containerid + "/forms/processes/" + processid + "/content");
        return "kieserverdialect :: startprocessmodal";
    }

    @RequestMapping("/ksdworkontask/{tid}/{cid}")
    public String workOnTask(@PathVariable("tid") String taskid,
                                       @PathVariable("cid") String containerid,
                                       Model model) {
        model.addAttribute("containerid",
                           containerid);
        model.addAttribute("taskid",
                           taskid);
        model.addAttribute("formlocation",
                           "/rest/server/containers/" + containerid + "/forms/tasks/" + taskid + "/content");
        return "kieserverdialect :: workontaskmodal";
    }
}
