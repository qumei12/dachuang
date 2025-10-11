package com.wangyan.servlets;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dbhelper.DBSearch;
import javabean.Disease;
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
		ArrayList<Disease> diseaseList = dbs.getMashupTable(startId, count);
		
		// 将Disease对象转换为DiseaseJson对象
		ArrayList<DiseaseJson> mashupList = new ArrayList<DiseaseJson>();
		for (Disease disease : diseaseList) {
			DiseaseJson diseaseJson = new DiseaseJson();
			diseaseJson.setN_ID(disease.getID());
			diseaseJson.setC_NAME(disease.getNAME());
			diseaseJson.setC_DESCRIPTION(disease.getDESCRIPTION());
			mashupList.add(diseaseJson);
		}
		
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
		doGet(request, response);
	}

}