package com.wangyan.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.wangyan.business.Search;

import javabean.Mashup;
import javabean.Mashup_json;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * Servlet implementation class MashupTable
 */
public class MashupTable extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public MashupTable() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		//response.getWriter().append("Served at: ").append(request.getContextPath());
		String page_str = request.getParameter("page");
		int page = Integer.parseInt(page_str);
		//System.out.println(page);
		ArrayList<Mashup_json> list = Search.searchMashupTable(page);
		//System.out.println(list);
		JSONObject jsonObject = new JSONObject();
		jsonObject.accumulate("mashupList", JSONArray.fromObject(list));
		
		JSONObject object = new JSONObject();
		object.accumulate("resultValue", jsonObject.toString());
		
		PrintWriter out = response.getWriter(); 
		out.write(object.toString());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
