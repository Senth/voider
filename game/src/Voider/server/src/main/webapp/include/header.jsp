<!doctype html>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%!
	private String currentPage = null;
	private String rootUrl = "";
	
	private void setCurrentPage(String page) {
		// Remove everything after the jsp
		int jspLocation = page.indexOf(".jsp");
		if (jspLocation != -1) {
			// Remove first slash too
			page = page.substring(1, jspLocation + 4);
		} else {
			page = "";
		}
		
		currentPage = page;
	}
	
	private void setRootUrl(String url) {
		int currentPageLocation = url.indexOf(currentPage);
		if (currentPageLocation != -1) {
			rootUrl = url.substring(0, currentPageLocation);
		}
	}
	
	private String link(String url) {
		return rootUrl + url;
	}
	
	private boolean isCurrentPage(String url) {
		return currentPage.equals(url);
	}
	
	private String printIfPage(String url, String isPage, String notPage) {
		return isCurrentPage(url) ? isPage : notPage;
	}
%>

<%
	setCurrentPage(request.getRequestURI());
	setRootUrl(request.getRequestURL().toString());
%>

<html>
  <head>
    <title>VOIDER</title>
    <meta name="viewport" content="width=device-width">
	<link rel="stylesheet" href="<%= link("css/voider.css") %>">
	<link rel="stylesheet" href="<%= link("css/glyphicons.css") %>">
	<script src="<%= link("scripts/modernizr.js") %>"></script>
    <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/2.0.3/jquery.min.js"></script>
    <script type="text/javascript" src="https://netdna.bootstrapcdn.com/bootstrap/3.1.1/js/bootstrap.min.js"></script>
  </head>
  
  <body>
    <div class="container">
      <div class="row">
        <div class="col-md-12" style="padding: 40px 0px 40px 0px;">
          <div class="navbar navbar-default">
            <div class="container">
              <div class="navbar-header">
                <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
                  <span class="sr-only">Toggle navigation</span><span class="icon-bar"></span><span class="icon-bar"></span><span class="icon-bar"></span>
                </button>
                <a href="<%= link("index.jsp") %>" class="navbar-brand">VOIDER</a>
              </div>
              <div class="collapse navbar-collapse pull-right">
                <ul class="nav navbar-nav">
                  <li class="<%= printIfPage("index.jsp", "active", "") %>">
                    <a href="<%= link("index.jsp") %>">Home</a>
                  </li>
                  <li class="<%= printIfPage("beta.jsp", "active", "") %>">
                    <a href="<%= link("beta.jsp") %>">Beta Testing</a>
                  </li>
                </ul>
              </div>
            </div>
          </div>
        </div>
      </div>