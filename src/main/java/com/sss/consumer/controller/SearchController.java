package com.sss.consumer.controller;

import com.sss.consumer.DubboServices;
import javafx.util.Pair;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Controller
public class SearchController extends CommonPageController{
    @RequestMapping(value = "/search")
    public ModelAndView get(ModelMap m, HttpSession session) throws ServletException, IOException {

        ModelAndView mv=super.get(m,"search",session);
        mv.addObject("info","输入关键词开始搜索");
        return mv;
    }
    @RequestMapping(value = "/search/{category}/{keyword}")
    public ModelAndView dosearch(ModelMap m, HttpSession session, @PathVariable String keyword, @PathVariable String category) throws ServletException, IOException {

        ModelAndView mv=super.get(m,"search",session);
//        List<Pair<String, List<Pair<String, Pair<String, String>>>>> limits = new LinkedList<Pair<String, List<Pair<String, Pair<String, String>>>>>();
//        LinkedList<Pair<String, Pair<String, String>>> fir=new LinkedList<Pair<String, Pair<String, String>>>();
//        fir.add(new Pair("instruction",new Pair("matchQuery","Machine")));
//        limits.add(new Pair("abstract",fir));
        if(keyword.length()==0)return mv;
        String index="abstract";
        if(category.equals("expert"))index="name";
        List<Map<String, Object>> res = DubboServices.INSTANCE.esService.FuzzyQuery(category,index,keyword, 100);
        if (res == null) {
            mv.addObject("info","无查询结果");
        }else{
            mv.addObject("info","");
            mv.addObject("res",res);
        }
        return mv;
    }






}
