<?xml version="1.0" encoding="utf-8"?>
<!DOCTYPE stylesheet [
<!ENTITY ø "oslash">
<!ENTITY å "aring">
<!ENTITY æ "aelig">
]>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml" version="1.0" doctype-public="http://www.daisy.org/z3986/2005/dtbook-2005-3.dtd" encoding="UTF-8" indent="yes"/>

	<xsl:output method="xml" omit-xml-declaration="yes" indent="yes"/>

	<xsl:variable name="node" select="document('invalidNode.xml')/article/element"/>
	<xsl:variable name="index" select="document('invalidNode.xml')/article/index"/>

	<xsl:strip-space elements="*"/>

	<xsl:template match="@* | node()">
		<xsl:copy>
			<xsl:apply-templates select="@* | node()"/>
		</xsl:copy>
	</xsl:template>

	

	<xsl:template match="node()[$node][$index]">
<!--		<xsl:if test="self::.[$node='level3'][$index]">
		</xsl:if>-->
		<xsl:value-of select="$node"/>
		<xsl:value-of select="$index"/>
	</xsl:template>
</xsl:stylesheet><!-- Stylus Studio meta-information - (c) 2004-2007. Progress Software Corporation. All rights reserved.
<metaInformation>
<scenarios/><MapperMetaTag><MapperInfo srcSchemaPathIsRelative="yes" srcSchemaInterpretAsXML="no" destSchemaPath="" destSchemaRoot="" destSchemaPathIsRelative="yes" destSchemaInterpretAsXML="no"/><MapperBlockPosition></MapperBlockPosition><TemplateContext></TemplateContext><MapperFilter side="source"></MapperFilter></MapperMetaTag>
</metaInformation>
-->