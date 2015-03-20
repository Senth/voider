<!doctype html>
<%@include file="methods.jsp" %>

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