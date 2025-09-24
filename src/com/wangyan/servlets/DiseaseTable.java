package com.wangyan.servlets;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dbhelper.DBSearch;
import javabean.DiseaseJson;

/**
 * Servlet implementation class MashupTable
 */
public class DiseaseTable extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DiseaseTable() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		int page = 1;
		if(request.getParameter("page") != null){
			page = Integer.parseInt(request.getParameter("page"));
		}
		int startId = (page - 1) * 20;
		int count = 20;
		DBSearch dbs = new DBSearch();
		ArrayList<DiseaseJson> mashupList = dbs.getMashupTable(startId, count);
		int pageAmount = dbs.getPageAmount();
		request.setAttribute("mashupList", mashupList);
		request.setAttribute("pageAmount", pageAmount);
		request.setAttribute("page", page);
		request.getRequestDispatcher("searchResult.jsp").forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}