<!DOCTYPE html>

<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>NootBot</title>
</head>
<body>
    <c:if test="${not null category}">
    	<h1>${category}</h1>
    </c:if>
    <c:if test="${not empty sounds}">
    	<ul>
			<c:forEach var="sound" items="${sounds}">
				<li>${sound.getSoundFileId()}</li>
			</c:forEach>
		</ul>
    </c:if>
</body>
</html>