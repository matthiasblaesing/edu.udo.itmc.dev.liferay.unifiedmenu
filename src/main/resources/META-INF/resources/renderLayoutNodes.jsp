<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://liferay.com/tld/ui" prefix="liferay-ui" %>
<%@ taglib uri="http://liferay.com/tld/util" prefix="liferay-util" %>
<c:set var="localLayoutNodes" scope="page" value="${layoutNodes}"/>
<ul>
    <c:forEach items="${localLayoutNodes}" var="layoutNode">
        <c:set var="localLayoutNode" scope="page" value="${layoutNode}"/>
        <li>
            <c:out value="${layoutNode.title}" /> (<c:out value="${layoutNode.friendlyUrl}" />)
            <c:if test="${layoutNode.friendlyNameConflict}">
                <div style="color: firebrick;"><liferay-ui:message key="friendlyUrlConflict" /></div>
            </c:if>
            <% request.setAttribute("layoutNodes", ((edu.udo.itmc.dev.liferay.unifiedmenu.pubpriv.MoveToPublicPortlet.LayoutNode) pageContext.getAttribute("localLayoutNode")).getChildren()); %>
            <liferay-util:include
                page="/META-INF/resources/renderLayoutNodes.jsp"
                servletContext="<%= application %>"
            />
        </li>
    </c:forEach>
</ul>
