package com.wangyan.business;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javabean.Supply;
import javabean.Disease;
import dbhelper.DBSearch;

/**
 * Servlet implementation class Search
 */
public class Search extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Search() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String diseaseName = request.getParameter("search");
		DBSearch dbs = new DBSearch();
		ArrayList<Disease> diseases = dbs.getDiseaseByNameFuzzy(diseaseName);
		ArrayList<Supply> supplies = new ArrayList<Supply>();
		if(diseases.size() > 0){
			supplies = dbs.getDiseaseSupplyRelation(diseases.get(0).getID());
		}
		request.setAttribute("diseases", diseases);
		request.setAttribute("supplies", supplies);
		request.getRequestDispatcher("searchResult.jsp").forward(request, response);
	}

}