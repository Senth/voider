<%@include file="include/header.jsp" %>
<div class="row">
	<div class="col-md-12">
		<h1 class="text-center">Available Beta Keys</h1>
	</div>
</div>
<hr />
<div class="row">
	<div class="col-md-12">
		<h3>${group}</h3>
		<table>
			<c:forEach items="${keys}" var="key" varStatus="loopStatus">
				<tr>
					<td style="width: 50px;">${loopStatus.index + 1}:</td>
					<td style="color: #ffffff;">${key}</td>
				</tr>
			</c:forEach>
		</table>
	</div>
</div>
<jsp:include page="include/footer.jsp" />