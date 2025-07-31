package com.wangyan.servlets;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.wangyan.business.Search;

import javabean.API;

/**
 * Servlet implementation class MashupApiRelation
 */
public class MashupApiRelation extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public MashupApiRelation() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//response.getWriter().append("Served at: ").append(request.getContextPath());
		//System.out.println("shoudao!!!!");
		String mashupId_string = request.getParameter("mashupId");
		String mashupName = request.getParameter("mashupName");
		int mashupId = Integer.parseInt(mashupId_string);
		ArrayList<API> list = Search.searchMashupApiRelation(mashupId);
		
		request.setAttribute("apilist", list);
		request.setAttribute("mashupName", mashupName);
		
		request.getRequestDispatcher("/pages/mashupDetail.jsp").forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
