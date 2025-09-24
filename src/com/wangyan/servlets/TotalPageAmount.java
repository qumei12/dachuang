package com.wangyan.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dbhelper.DBSearch;

import net.sf.json.JSONObject;

/**
 * Servlet implementation class TotalPageAmount
 */
public class TotalPageAmount extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public TotalPageAmount() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		//response.getWriter().append("Served at: ").append(request.getContextPath());
		DBSearch dbs = new DBSearch();
		int amount = dbs.getPageAmount();
		JSONObject jsonObject = new JSONObject();
		jsonObject.accumulate("pageAmount", amount);
		
		JSONObject object = new JSONObject();
		object.accumulate("resultValue", jsonObject);
		
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