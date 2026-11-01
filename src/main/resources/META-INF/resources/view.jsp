<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib uri="http://liferay.com/tld/ui" prefix="liferay-ui" %>
<%@ taglib uri="http://liferay.com/tld/util" prefix="liferay-util" %>

<div class="container-fluid container-fluid-max-xl main-content-body mt-4">

    <div class="sheet sheet-lg custom-sheet">
        <portlet:actionURL name="execute" var="executeURL" copyCurrentRenderParameters="true"/>

        <c:choose>
            <c:when test="${not empty moveToPublicLayoutTree}">
                <form action="${executeURL}" method="POST">
                    <h1>Move to public</h1>

                    <p><liferay-ui:message key="usage" /><p>

                    <c:if test="${moveToPublicLayoutTree.get(0).isFriendlyNameConflictRecursive()}">
                        <div class="alert alert-warning">
                            <liferay-ui:message key="friendlyUrlConflictTotal" />
                        </div>
                    </c:if>

                    <p>
                        <input type="checkbox" value="true" name="<portlet:namespace/>removeSource" id="<portlet:namespace/>removeSource" style="margin-right: 1ex"/>
                        <label for="<portlet:namespace/>removeSource"><liferay-ui:message key="remove-source" /></label>
                    </p>

                    <div class="sheet-footer">
                        <div class="btn-group-item">
                            <div class="btn-group-item">
                                <button class="btn btn-primary" type="submit" <c:if test="${moveToPublicLayoutTree.get(0).isFriendlyNameConflictRecursive()}">disabled="disabled"</c:if>>
                                        <span class="lfr-btn-label">
                                        <liferay-ui:message key="move-to-public" />
                                    </span>
                                </button>
                            </div>
                        </div>
                    </div>

                </form>

                <h2><liferay-ui:message key="moved-tree" /></h2>

                <p>
                    <liferay-ui:message key="new-parent" />:
                    <c:if test="${not empty moveToPublicParentLayout}">
                        ${moveToPublicParentLayout.getNameCurrentValue()} (${moveToPublicParentLayout.getGroup().getFriendlyURL()}${moveToPublicParentLayout.getFriendlyURL()})
                    </c:if>
                    <c:if test="${empty moveToPublicParentLayout}">
                        <liferay-ui:message key="root-level" />
                    </c:if>
                </p>

                <% request.setAttribute("layoutNodes", request.getAttribute("moveToPublicLayoutTree")); %>
                <liferay-util:include
                    page="/META-INF/resources/renderLayoutNodes.jsp"
                    servletContext="<%= application %>"
                />
            </c:when>
            <c:otherwise>
                <p><liferay-ui:message key="layout-not-found" /></p>
            </c:otherwise>
        </c:choose>
    </div>
</div>
