<%@include file="../include/header.jsp" %>
<%@page
import="com.google.appengine.api.blobstore.BlobstoreService, com.google.appengine.api.blobstore.BlobstoreServiceFactory"
%>
<%
BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
%>
	<div class="row">
		<div class="col-md-12">
			<h1 class="text-center">Fix Blobs</h1>
		</div>
	</div>
	<hr />
	<div class="row">
		<div class="col-md-12">
			<h3 class="text-Left">Upload Resource</h3>
			<form method="post" action="<%= blobstoreService.createUploadUrl("/admin/fixit") %>" enctype="multipart/form-data">
				<p>
					<span style="float: left; width: 100px;">Resource:</span>
					<input type="file" name="resource" style="width: 200px;"/>
				</p>
				<p>
					<span style="float: left; width: 100px;">UUID:</span>
					<input type="text" name="uuid" style="width: 200px;"/>
				</p>
				<p>
					<input type="submit" />
				</p>
			</form>
		</div>
	</div>
<jsp:include page="../include/footer.jsp" />