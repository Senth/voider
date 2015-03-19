<%@include file="include/header.jsp" %> 
      <div class="row">
        <div class="col-md-12" style="padding: 0px 0px 20px 0px;">
          <h1 class="text-center"><%
          	// Success
          	if (request.getParameter("success") != null) {
          		out.print("Success!");
          	} else if (request.getParameter("key_gotten") != null) {
          		out.print("You have already entered the beta :)");
          	} else if (request.getParameter("in_queue") != null) {
          		out.print("This email seems to be in the beta queue already");
          	} else if (request.getParameter("not_confirmed") != null) {
          		out.print("This email has already entered the queue<br />But hasn't been confirmed yet");
          	} else if (request.getParameter("confirm_success") != null) {
				out.print("Confirmation successful!");
          	} else if (request.getParameter("resend_confirm") != null) {
          		out.print("Confirmation mail resent");
          	} else if (request.getParameter("resend_key") != null) {
          		out.print("Beta key was sent to your email");
          	}
          %></h1>
        </div>
      </div>
      <%
      	if (request.getParameter("success") != null) {
      		out.println("<hr />");
      		out.println("<div class=\"row\">");
      		out.println("<div class=\"col-md-12\">");
      		out.println("<h3 class=\"text-center\">Please confirm your email within a week, or you'll automatically leave the queue</h3>");
      		out.println("</div></div>");
      	}
      	// Resend key
      	else if (request.getParameter("key_gotten") != null) {
      		String email = request.getParameter("email");
      		if (email != null) {
	      		out.println("<hr />");
	      		out.println("<div class=\"row\">");
	      		out.println("<div class=\"col-md-12\">");
	      		out.println("<h3 class=\"text-center\"><a href=\"beta-signup?resend_key&email=" + email + "\">Resend Key</a></h3>");
	      		out.println("</div></div>");
      		}
      	}
      	// Resend confirmation mail
      	else if (request.getParameter("not_confirmed") != null) {
      		String email = request.getParameter("email");
      		if (email != null) {
	      		out.println("<hr />");
	      		out.println("<div class=\"row\">");
	      		out.println("<div class=\"col-md-12\">");
	      		out.println("<h3 class=\"text-center\"><a href=\"beta-signup?resend_confirm&email=" + email + "\">Resend Key</a></h3>");
	      		out.println("</div></div>");
      		}
      	}
      %>
      <hr />
      <div class="row">
      	<div class="col-md-12">
      		<h4 class="text-center"><a href="beta.jsp">Back</a></h4>
      	</div>
      </div>
<%@include file="include/footer.jsp" %>