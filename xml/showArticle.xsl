<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="html" version="4.01" doctype-public="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd" encoding="UTF-8" indent="yes"/>

	<xsl:variable name="id" select="document('uniqueNames.xml')/category/group/@id"/>
	<xsl:variable name="currentUniqueName" select="document('currentUniqueName.xml')/uniqueName"/>

	<xsl:template match="ingress[.!=' ']">
		<p>
			<em>
				<xsl:apply-templates/>
			</em>
		</p>
	</xsl:template>

	<xsl:template match="tittel">
		<h2>
			<xsl:apply-templates/>
		</h2>
	</xsl:template>

	<xsl:template match="feed">
		<html>
			<head>
				<title>Eksempler på currentUniqueName</title>
				<link href="../css/article.css" rel="stylesheet" type="text/css" media="all"/>
			</head>
			<body>
				<xsl:if test="$currentUniqueName[1]">
					<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName]">
						<xsl:apply-templates select="../../innhold"/>
					</xsl:for-each>
				</xsl:if>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet><!-- Stylus Studio meta-information - (c) 2004-2007. Progress Software Corporation. All rights reserved.
<metaInformation>
<scenarios/><MapperMetaTag><MapperInfo srcSchemaPathIsRelative="yes" srcSchemaInterpretAsXML="no" destSchemaPath="" destSchemaRoot="" destSchemaPathIsRelative="yes" destSchemaInterpretAsXML="no"/><MapperBlockPosition></MapperBlockPosition><TemplateContext></TemplateContext><MapperFilter side="source"></MapperFilter></MapperMetaTag>
</metaInformation>
-->