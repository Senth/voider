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
          <p>So you've gotten hand on the Voider beta. What do you do now? If it's still in the early stages of the beta there might not be many levels to play. In that case my suggestion for you is to:</p>
          <ol>
          <li>Look at the first <a href="#tutorials">7 YouTube tutorials</a>.</li>
          <li>Check out useful <a href="#hotkeys">Hotkeys</a>.</li>
          <li>When you're comfortable enough continue watching the tutorials and adding enemies to your level.</li>
          <li>Publish your level when it's done</li>
          </ol>
          <p>Levels that are liked by the community will get transferred to the released version of the game :) Nothing else will be transfered to the release version as it looks now</p>
        </div>
      </div>
      <hr />
      <div class="row">
        <div class="col-md-12">
          <h3 class="text-Left">Upcoming features</h3>
          <ol>
          </ol>
        </div>
      </div>
      <hr />
      <div class="row">
        <div class="col-md-12">
          <h3 class="text-Left">Known Problems</h3>
          <ol>
          	<li>No possibility to delete resources</li>
          </ol>
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
      <hr />
      <div id="hotkeys" class="row">
        <div class="col-md-12">
          <h3 class="text-Left">Hotkeys</h3>
          <h4>Anywhere</h4>
          <ul>
          <li><span class="def">Toggle Fullscreen</span>Alt + Enter</li>
          <li><span class="def">Report Bug/Feature Request Window</span>Insert</li> 
          </ul> 
          <h4>Editors</h4>
          <ul>
          <li><span class="def">Pan Screen</span>Middle Mouse Button OR Space + Left Mouse</li>
          <li><span class="def">Zoom in/out</span>Ctrl + Scroll Up/Down</li>
          <li><span class="def">Undo</span>Ctrl + Z</li>
          <li><span class="def">Redo</span>Ctrl + Y OR Ctrl + Shift + Z</li>
          <li><span class="def">Save</span>Ctrl + S</li>
          </ul>
          
          <h4>Editor tools</h4>
          The tooltip will display the hotkey for the tool if it has any (e.g. "Draw Terrain (D)" )

        </div>
      </div>
<jsp:include page="include/footer.jsp" />