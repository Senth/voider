<%@include file="include/header.jsp" %>
      <div class="row">
        <div class="col-md-12" style="padding: 0px 0px 40px 0px;">
          <h1 class="text-center">Beta Testing</h1>
        </div>
      </div>
      <hr />
      <div class="row">
        <div class="col-md-12">
          <h3 class="text-Left"><%
          	// Display sign up
          	if (request.getParameter("signed_up") == null) {
          		out.print("Sign up here");
          	} else {
          		out.print("Please confirm your beta sign-up through your mail :)");
          	}
          	%></h3>
          	<% if (request.getParameter("signed_up") == null) {
          		out.print("<form action=\"beta-signup\" method=\"post\"><p class=\"text-left\"><span style=\"margin-right: 10px\">Email:</span><input type=\"text\" name=\"email\" style=\"width: 200px;\" /><input type=\"submit\" /></p></form>");
	        }%>
        	You will receive an email with a <b>confirmation link</b>. You will need to confirm your email through that link within 1 week, otherwise your position will be removed. You will then have to wait in queue to get a key. New keys are released every now and then :)
        </div>
      </div>
      <div class="row">
        <div class="col-md-12" style="padding-top: 20px">
          <h3 class="text-Left"><a href="http://storage.googleapis.com/voider-shared/app/Voider-beta.jar">Download Beta</a></h3>
			*Requires beta access to play*
        </div>
      </div>
      <hr />
      <div id="tutorials" class="row">
        <div class="col-md-12">
          <h3 class="text-Left"><a href="https://youtu.be/KRPMoLZ2ZN8">Tutorial On Creating Your Own Content</a></h3>
        </div>
      </div>
      <hr />
      <div class="row">
      	<div class="col-md-12" style="padding: 40px 0px 40px 0px;">
      		<h2 class="text-center">Helpful Information for Beta Testers</h2>
      	</div>
      </div>
      <hr />
      <div class="row">
        <div class="col-md-12">
          <h3 class="text-Left">Start Here</h3>
          <p>So you've gotten hand on the Voider beta. What do you do now?</p>
          <ol>
          <li>Play some levels</li>
          <li>Create you own content by watching the <a href="https://youtu.be/KRPMoLZ2ZN8">YouTube tutorials</a>.</li>
          <li>Publish your level when it's done</li>
          </ol>
          <p>Levels that are liked by the community might get transferred to the released version of the game :) Nothing else will be transfered to the release version as it looks now</p>
        </div>
      </div>
      <hr />
      <div class="row">
        <div class="col-md-12">
          <h3 class="text-Left">Report a Bug</h3>
          <ul>
          	<li>You can report a bug in the desktop client by pressing 'Insert' or</li>
          	<li>In all editors, clicking the bug-report icon (to the right of undo/redo at the top bar)</li>
          </ul> 
        </div>
      </div>
<jsp:include page="include/footer.jsp" />