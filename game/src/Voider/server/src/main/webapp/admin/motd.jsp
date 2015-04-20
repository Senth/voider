<%@include file="../include/header.jsp" %> 
	<div class="row">
		<div class="col-md-12">
			<h1 class="text-center">Server Messages (MOTDs)</h1>
		</div>
	</div>
	<hr />
	<div class="row">
		<div class="col-md-12">
			<h3 class="text-Left">Create New Message</h3>
			<form method="post" action="<%= link("admin/motd") %>">
				<p>
					<span style="float: left; width: 100px;">Expires:</span>
					<input type="text" name="expires" value="${default.expires}" style="width: 200px;" />
					<span style="padding-left: 25px; color: #ff2222;">${default.expiresError}</span>
				</p>
				<p>
					<span style="float: left; width: 100px;">Title:</span>
					<input type="text" name="title" value="${default.title}" style="width: 200px;" />
					<span style="padding-left: 25px; color: #ff2222;">${default.titleError}</span>
				</p>
				<p>
					<span style="float: left; width: 100px;">Level:</span>
					<select name="level">
						<option value="SEVERE">Severe</option>
						<option value="WARNING">Warning</option>
						<option value="HIGHLIGHT">Highlight</option>
						<option value="INFO" selected="selected">Info</option>
						<option value="FEATURED">Featured</option>
					</select>
					<span style="padding-left: 25px; color: #ff2222;">${default.levelError}</span>
				</p>
				<p>
					<span style="float: left; width: 100px;">Message:</span>
					<textarea name="message" value="${default.message}" style="width: 400px;" rows="10"></textarea>
					<span style="padding-left: 25px; color: #ff2222;">${default.messageError}</span>
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
			<h3 class="text-Left">Messages</h3>
			<table>
				<tr>
					<th>Created</th>
					<th>Expires</th>
					<th>Level</th>
					<th>Title</th>
					<th>Message</th>
				</tr>
				<c:set var="rowStyleDefault" value="padding: 2px 5px;"/>
				<c:forEach items="${messages}" var="motd">
					<c:choose>
						<c:when test="${motd.expired}">
							<c:set var="rowStyle" value="${rowStyleDefault} color: #888888;"/>
						</c:when>
						
						<c:otherwise>
							<c:set var="rowStyle" value="${rowStyleDefault} color: #00ffff;"/>
						</c:otherwise>
					</c:choose>
					<tr>
						<td style="${rowStyle}">${motd.createDate}</td>
						<td style="${rowStyle}">${motd.expireDate}</td>
						<td style="${rowStyle}">${motd.type}</td>
						<td style="${rowStyle} text-align: center;">${motd.title}</td>
						<td style="${rowStyle}">${motd.message}</td>
					</tr>
				</c:forEach>
			</table>
		</div>
	</div>
<jsp:include page="../include/footer.jsp" />