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