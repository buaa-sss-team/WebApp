package com.sss.consumer.controller;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sss.consumer.DubboServices;
import com.sss.consumer.FileUtil;
import com.sss.interfaces.hmodel.Expert;
import com.sss.interfaces.hmodel.Paper;
import com.sss.interfaces.hmodel.Patent;
import com.sss.interfaces.hmodel.User;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class ResourcesPageController extends CommonPageController{

    private boolean isBought(boolean isPaper, int id, User user) {
        if(user==null)return false;
        Gson gson = new Gson();
        JsonArray boughtItems = gson.fromJson(user.getBoughtThings(), JsonArray.class);
        for (int i = 0; i < boughtItems.size(); i++) {
            if(isPaper&&boughtItems.get(i).getAsJsonObject().get("type").getAsString().equals("paper")
                    &&id==boughtItems.get(i).getAsJsonObject().get("id").getAsInt()){
                return true;
            }
            if((!isPaper)&&boughtItems.get(i).getAsJsonObject().get("type").getAsString().equals("patent")
                    &&id==boughtItems.get(i).getAsJsonObject().get("id").getAsInt()){
                return true;
            }
        }
        return false;
    }

    @RequestMapping(value = "/paper/{id}", method = RequestMethod.GET)
    public ModelAndView getPaper(ModelMap m, @PathVariable String id, HttpSession session)throws ServletException, IOException {
        ModelAndView mv=get(m,"paper",session);
        Paper paper=DubboServices.INSTANCE.commonService.getPaperInfo(Integer.valueOf(id));
        mv.addObject("paper", paper);
        mv.addObject("bought",isBought(true,Integer.valueOf(id),DubboServices.INSTANCE.commonService.getUserInfo((String)session.getAttribute("currentUserName"))));
        Gson gson = new Gson();
        String[] urls=gson.fromJson(paper.getUrl(),String[].class);
        mv.addObject("urls",urls);
        //TODO @母宇婷 看下面
        String[] keywords=gson.fromJson(paper.getKeywords(),String[].class);
        String keywordStr="";
        if(keywords!=null)
            for (String word:keywords)keywordStr+=", "+word;
        if(keywordStr.length()>2)mv.addObject("keywords",keywordStr.substring(2));

        String authorsStr="";
        JsonArray authors=gson.fromJson(paper.getAuthorId(),JsonArray.class);
        if(authors!=null)
            for(JsonElement author:authors){
                authorsStr+=", "+author.getAsJsonObject().get("name").getAsString();
            }
        if(authorsStr.length()>2)mv.addObject("authors",authorsStr.substring(2));
        //TODO @母宇婷 看上面
        return mv;
    }

    @RequestMapping(value = "/paper/{id}/download/{filename:.+}", method = RequestMethod.GET)
    public ResponseEntity<byte[]> download(@PathVariable("filename") String filename, HttpServletRequest req, @PathVariable String id,HttpSession session) throws ServletException,IOException{
        //ApplicationContext ctx = new ClassPathXmlApplicationContext("spring-consumer.xml");
        //fileService = ctx.getBean(FileService.class);
        Paper paper=DubboServices.INSTANCE.commonService.getPaperInfo(Integer.valueOf(id));
        if(isBought(true,Integer.valueOf(id),DubboServices.INSTANCE.commonService.getUserInfo((String)session.getAttribute("currentUserName"))))
            return FileUtil.FileDownload(filename);
        else return null;
    }


    @RequestMapping(value = "/expert/{id}", method = RequestMethod.GET)
    public ModelAndView getExpert(ModelMap m, @PathVariable String id, HttpSession session)throws ServletException, IOException {
        ModelAndView mv=get(m,"expert",session);
        Expert exp=DubboServices.INSTANCE.commonService.getExpertInfo(Integer.valueOf(id));
        mv.addObject("expert", exp);
        mv.addObject("bought",isBought(true,Integer.valueOf(id),DubboServices.INSTANCE.commonService.getUserInfo((String)session.getAttribute("currentUserName"))));

        Gson gson=new Gson();
        String[] orgarr=gson.fromJson(exp.getOrgs(),String[].class);
        String orgStr="";
        if(orgarr!=null)
            for (String word:orgarr)orgStr+=", "+word;
        if(orgStr.length()>2)mv.addObject("orgs",orgStr.substring(2));

        String tagStr="";
        JsonArray authors=gson.fromJson(exp.getTags(),JsonArray.class);
        int tagCount=0;
        if(authors!=null)
            for(JsonElement author:authors){
                tagStr+=", "+author.getAsJsonObject().get("t").getAsString();
                if(tagCount++>30)break;
            }
        if(tagStr.length()>2)mv.addObject("tags",tagStr.substring(2));


        List<Map<String,String>> pubs=gson.fromJson(exp.getPubid(), new TypeToken<List<Map<String, String>>>() {
        }.getType());;
        mv.addObject("pubs",pubs);


        return mv;

    }
}
