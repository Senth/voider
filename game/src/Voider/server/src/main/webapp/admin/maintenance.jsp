<%@include file="../include/header.jsp" %> 
	<div class="row">
		<div class="col-md-12">
			<h1 class="text-center">Maintenance</h1>
		</div>
	</div>
	<hr />
	<div class="row">
		<div class="col-md-12">
			<form method="post" action="<%= link("admin/maintenance") %>">
			<c:choose>
				<c:when test="${maintenance_mode == 'UP'}">
					<h3 class="text-Left" style="color: #2a2;">Server is UP!</h3>
					<p>Reason:</p>
					<textarea name="reason" style="min-width: 500px; min-height: 300px;"></textarea><br />
					<input type="hidden" name="maintenance_mode" value="DOWN"/>
					<input type="submit" style="background-color: #a22; border-color: #700; color: #fff;" value="Deactivate Server" />
				</c:when>
				<c:when test="${maintenance_mode == 'DOWN'}">
					<h3 class="text-Left" style="color: #a22;">Server is DOWN!</h3>
					<input type="hidden" name="maintenance_mode" value="UP"/>
					<input type="submit" style="background-color: #2a2; border-color: #070; color: #fff;" value="Activate Server" />
				</c:when>
			</c:choose>
			</form>
		</div>
	</div>
	<hr />
	<div class="row">
		<div class="col-md-12">
			<h1 class="text-center">Backup</h1>
		</div>
	</div>
	<hr />
	<div class="row">
		<div class="col-md-12">
			<h3 class="text-Left">Backups</h3>
			<form method="post" action="<%= link("admin/maintenance") %>">
			<table>
			<tr>
				<td></td>
				<td><strong>Date</strong></td>
			</tr>
				<c:forEach items="${backup_points}" var="backup_point">
					<tr>
						<td><input type="radio" name="backup_id" value="${backup_point.id}" /></td>
						<td>${backup_point.date}</td>
					</tr>
				</c:forEach>
			</table>
			<input type="Submit" value="Create Restore Point"/>
			</form>
		</div>
	</div>
	<hr />
	<div class="row">
		<div class="col-md-12">
			<h3 class="text-Left">Previous restore dates</h3>
			<table>
			<tr>
				<td><strong>From</strong></td>
				<td><strong>To</strong></td>
			</tr>
			<c:forEach items="${restored_dates}" var="restore_date">
				<tr>
					<td>${restore_date.from}</td>
					<td>${restore_date.to}</td>
				</tr>
			</c:forEach>
			</table>
		</div>
	</div>	
<jsp:include page="../include/footer.jsp" />
