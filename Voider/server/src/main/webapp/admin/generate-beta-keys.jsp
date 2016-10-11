<%@include file="../include/header.jsp" %> 
	<div class="row">
		<div class="col-md-12">
			<h1 class="text-center">Generate Beta Keys</h1>
			<c:if test="${not empty responseStatus}">
				<p class="text-center ${responseStatus.type}">${responseStatus.message}</p>
			</c:if>
		</div>
	</div>
	<hr />
	<div class="row">
		<div class="col-md-12">
			<h3 class="text-Left">Send beta keys to emails</h3>
			<form method="post" action="<%= link("admin/generate-beta-keys") %>">
				<p>Paste email addresses here (one per row)</p>
				<p>
					<textarea name="email_list" rows="8" cols="50"></textarea>
				</p>
				<p>
					<input type="submit" />
				</p>
			</form>
		</div>
	</div>
	<hr />
	<div class="row">
		<div class="col-md-12">
			<h3 class="text-Left">Generate Beta Group</h3>
			<form method="post" action="<%= link("admin/generate-beta-keys") %>">
				<p>
					<span style="float: left; width: 100px;">Group Name:</span>
					<input type="text" name="group" style="width: 200px" />
				</p>
				<p>
					<span style="float: left; width: 100px;">How many?</span>
					<input type="text" name="count" style="width: 200px" />
				</p>
				<p>
					<input type="submit" />
				</p>
			</form>
		</div>
	</div>
	<hr />
	<div class="row">
		<div class="col-md-12">
			<h3 class="text-Left">Groups</h3>
			<table>
				<c:forEach items="${groups}" var="group">
					<tr>
						<td style="width: 150px;">${group.name}</td>
						<td><a href="${group.link}">${group.link}</a></td>
						<td style="padding-left: 20px;"><a href="<%= link("admin/generate-beta-keys?delete=") %>${group.name}">Delete</a></td>
					</tr>
				</c:forEach>
			</table>
		</div>
	</div>
<jsp:include page="../include/footer.jsp" />