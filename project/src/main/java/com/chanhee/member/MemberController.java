package com.chanhee.member;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

// 会員Controller.

@Controller
public class MemberController {
	
	MemberService memberService;
	BCryptPasswordEncoder passEncoder;
	
	@Autowired
	public MemberController(MemberService memberService, BCryptPasswordEncoder passEncoder) {
		super();
		this.memberService = memberService;
		this.passEncoder = passEncoder;
	}
	
	// 会員登録
	@RequestMapping("/member/memberjoin")
	public String register() {
		return "member/register";
	}
	
	@RequestMapping(value="/member/write" , method = RequestMethod.POST)
	public String wirte(@ModelAttribute MemberDTO member, Model model) {
		 MemberDTO dto = memberService.findByid(member.getId());
		 	if(dto != null) {
		     model.addAttribute("message", "이미 사용 중인 아이디입니다.");
		     return "member/register";
		    }
		 	// パスワード暗号化
		 	String encodepw = passEncoder.encode(member.getPw());
		    member.setPw(encodepw);
		 	memberService.insert(member);
		    return "redirect:/";
	}
	
	// ログイン
	@RequestMapping("/member/login")
	public String login() {
		return "member/login";
	}
	
	@RequestMapping(value="/member/loginpro" , method = RequestMethod.POST)
	public ModelAndView logincheck(@ModelAttribute MemberDTO memberDTO , HttpSession session) {
		String name = memberService.login(memberDTO, session);
		ModelAndView mav = new ModelAndView();
		if(name != null) {
			mav.setViewName("/index/index");
		}else {
			mav.setViewName("/member/login");
			mav.addObject("message", "아이디 또는 비밀번호가 틀립니다.");
		}
		return mav;
	}
	
	// ログアウト
	@RequestMapping("/member/logout")
	public ModelAndView logout(HttpSession session, ModelAndView mav) {
		memberService.logout(session);
		mav.setViewName("/index/index");
		mav.addObject("message", "logout");
		return mav;
	}
	
	@RequestMapping(value="/member/checkId", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Boolean> checkId(@RequestParam String id) {
	    MemberDTO member = memberService.findByid(id);
	    Map<String, Boolean> response = new HashMap<String, Boolean>();
	    response.put("exists", member != null);
	    return response;
	}
	
	// マイページ
	@RequestMapping(value="/member/memread", method = RequestMethod.GET)
	public String memread(@RequestParam("id") String id,@RequestParam("state") String state, Model model) {
		MemberDTO member = memberService.findByid(id);
		model.addAttribute("member",member);
		return "member/memread";
	}
	
	@RequestMapping("/member/update")
	public String memupdate(String id, Model model) {
		MemberDTO member = memberService.findByid(id);
		model.addAttribute("member",member);
		return "member/memupdate";
	}
	
	// 会員修正
	@RequestMapping(value="/member/updatepro" , method = RequestMethod.POST)
	public String memupdatepro(@ModelAttribute MemberDTO member) {
		
		if(member.getPw() != null && !member.getPw().isEmpty()) {
			String cpw = passEncoder.encode(member.getPw());
			member.setPw(cpw);
		}
		
		memberService.update(member);
		return "redirect:/";
	}
	
	// 会員削除
	@RequestMapping(value="/member/delete", method = RequestMethod.POST)
	public String memdelete(String id, HttpSession session) {
		memberService.delete(id);
		memberService.logout(session);
		return "redirect:/";
	}
	
	// パスワードの重複チェック
	@RequestMapping(value="/member/checkPw", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Boolean> checkPw(@RequestParam String id, @RequestParam String checkPw) {
	    MemberDTO member = memberService.findByid(id);
	    Map<String, Boolean> response = new HashMap<>();
	    
	    if (member != null && passEncoder.matches(checkPw, member.getPw())) {
	        response.put("exists", true);
	    } else {
	        response.put("exists", false);
	    }
	    
	    return response;
	}
	

}
