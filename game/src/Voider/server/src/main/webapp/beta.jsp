<!doctype html>

<html>
  
  <head>
    <title>VOIDER</title>
    <meta name="viewport" content="width=device-width">
	<link rel="stylesheet" href="css/voider.css">
	<link rel="stylesheet" href="css/glyphicons.css">
	<script src="scripts/modernizr.js"></script>
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
                <a href="index.jsp" class="navbar-brand">VOIDER</a>
              </div>
              <div class="collapse navbar-collapse pull-right">
                <ul class="nav navbar-nav">
                  <li>
                    <a href="index.jsp">Home</a>
                  </li>
                  <li class="active">
                    <a href="#">Beta Testing</a>
                  </li>
                </ul>
              </div>
            </div>
          </div>
        </div>
      </div>
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
          <h3 class="text-Left"><a href="Voider-beta.jar">Download Beta</a></h3>
			*Requires beta access to play*
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
      <hr />
      <div id="tutorials" class="row">
        <div class="col-md-12">
          <h3 class="text-Left">YouTube Tutorials (VERY OLD!!! Will be updated soon)</h3>
          <ol>
          <li><a href="https://www.youtube.com/watch?v=6qYusryW59w&list=PLth0PV80s30QXu67N1_DL6hZHVc8xZPQ8&index=1," target="blank">Introduction</a></li>
          <li><a href="https://www.youtube.com/watch?v=MiQCTPsKMS0&list=PLth0PV80s30QXu67N1_DL6hZHVc8xZPQ8&index=2," target="blank">Create a Level</a></li>
          <li><a href="https://www.youtube.com/watch?v=UbOF_KgpLzI&index=3&list=PLth0PV80s30QXu67N1_DL6hZHVc8xZPQ8," target="blank">Create Terrain</a></li>
          <li><a href="https://www.youtube.com/watch?v=7R1rK2b8jaU&index=4&list=PLth0PV80s30QXu67N1_DL6hZHVc8xZPQ8," target="blank">Selection Tool</a></li>
          <li><a href="https://www.youtube.com/watch?v=UFmqy6YWMRk&index=5&list=PLth0PV80s30QXu67N1_DL6hZHVc8xZPQ8," target="blank">Move Tool</a></li>
          <li><a href="https://www.youtube.com/watch?v=96S2M17STaI&list=PLth0PV80s30QXu67N1_DL6hZHVc8xZPQ8&index=6," target="blank">Delete Tool</a></li>
          <li><a href="https://www.youtube.com/watch?v=WThGYvHQSHU&list=PLth0PV80s30QXu67N1_DL6hZHVc8xZPQ8&index=7," target="blank">Panning</a></li>
          <li><a href="https://www.youtube.com/watch?v=MKDrEu0leYA&list=PLth0PV80s30QXu67N1_DL6hZHVc8xZPQ8&index=8," target="blank">Create a simple enemy</a></li>
          <li><a href="https://www.youtube.com/watch?v=q9BmR6E5JvM&index=9&list=PLth0PV80s30QXu67N1_DL6hZHVc8xZPQ8," target="blank">Enemy and bullet custom shapes</a></li>
          <li><a href="https://www.youtube.com/watch?v=whL_LQK64PA&index=10&list=PLth0PV80s30QXu67N1_DL6hZHVc8xZPQ8," target="blank">Enemy movement (Path &amp; Stationary)</a></li>
          <li><a href="https://www.youtube.com/watch?v=_k92tFhgiC8&index=11&list=PLth0PV80s30QXu67N1_DL6hZHVc8xZPQ8," target="blank">Enemy movement (AI)</a></li>
          <li><a href="https://www.youtube.com/watch?v=ZbKIrmIbrjk&list=PLth0PV80s30QXu67N1_DL6hZHVc8xZPQ8&index=12," target="blank">Enemy collision damage</a></li>
          <li><a href="https://www.youtube.com/watch?v=guOTjFNKRJU&index=13&list=PLth0PV80s30QXu67N1_DL6hZHVc8xZPQ8," target="blank">Bullet editor</a></li>
          <li><a href="https://www.youtube.com/watch?v=SxzoVL5YTHc&index=14&list=PLth0PV80s30QXu67N1_DL6hZHVc8xZPQ8," target="blank">Enemy weapons</a></li>
          <li><a href="https://www.youtube.com/watch?v=n3d31c4gaf8&list=PLth0PV80s30QXu67N1_DL6hZHVc8xZPQ8&index=15," target="blank">Adding enemies to a level</a></li>
          <li><a href="https://www.youtube.com/watch?v=DWeUECW8o2w&index=16&list=PLth0PV80s30QXu67N1_DL6hZHVc8xZPQ8," target="blank">Adding enemies with path movement to a level</a></li>
          <li><a href="https://www.youtube.com/watch?v=tzHQkAxrx94&list=PLth0PV80s30QXu67N1_DL6hZHVc8xZPQ8&index=17," target="blank">Spawn multiple enemies</a></li>
          <li><a href="https://www.youtube.com/watch?v=dM9aY8YqbAc&list=PLth0PV80s30QXu67N1_DL6hZHVc8xZPQ8&index=18," target="blank">Testing enemies in a level</a></li>
          <li><a href="https://www.youtube.com/watch?v=eCTuQdG3v98&index=19&list=PLth0PV80s30QXu67N1_DL6hZHVc8xZPQ8," target="blank">Enemy activate &amp; deactivate triggers</a></li>
          <li><a href="https://www.youtube.com/watch?v=jhUNPagEd7Y&list=PLth0PV80s30QXu67N1_DL6hZHVc8xZPQ8&index=20," target="blank">Miscellaneous tools and hotkeys</a></li>
          </ol>
        </div>
      </div>
      <hr />
      <div class="row">
        <div class="col-md-4" align="left">
          <a href="#">Facebook</a> | <a href="#">Twitter</a> | <a href="#">YouTube</a>
        </div>
        <div class="col-md-8">
          <div align="right">
            <a href="index.jsp">Home</a> | <a href="testing.html"> Beta Testing</a>
          </div>
        </div>
      </div>
      <div class="row">
        <div class="col-md-12" style="padding: 20px 0px 20px 0px;">
          <p class="text-center">Copyright &copy; <a href="#">Spiddekauga Games</a> 2015</p>
        </div>
      </div>
    </div>
  </body>

</html>