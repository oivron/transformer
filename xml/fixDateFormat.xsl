<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:template match="Workbook">
		<Workbook>
			<DocumentProperties>
				<LastAuthor>BoJo AS</LastAuthor>
			</DocumentProperties>
			<Worksheet>
				<Table>
					<xsl:apply-templates select="//Row"/>
				</Table>
			</Worksheet>
		</Workbook>
	</xsl:template>

	<xsl:template match="Row">
		<Row>
			<xsl:if test="child::Cell[1]">
				<xsl:apply-templates select="Cell[1]" mode="dateFormat"/>
			</xsl:if>
			<xsl:if test="child::Cell[2]">
				<xsl:apply-templates select="Cell[2]" mode="normal"/>
			</xsl:if>
			<xsl:if test="child::Cell[3]">
				<xsl:apply-templates select="Cell[3]" mode="normal"/>
			</xsl:if>
			<xsl:if test="child::Cell[4]">
				<xsl:apply-templates select="Cell[4]" mode="normal"/>
			</xsl:if>
			<xsl:if test="child::Cell[5]">
				<xsl:apply-templates select="Cell[5]" mode="normal"/>
			</xsl:if>
			<xsl:if test="child::Cell[6]">
				<xsl:apply-templates select="Cell[6]" mode="normal"/>
			</xsl:if>
			<xsl:if test="child::Cell[7]">
				<xsl:apply-templates select="Cell[7]" mode="normal"/>
			</xsl:if>
			<xsl:if test="child::Cell[8]">
				<xsl:apply-templates select="Cell[8]" mode="normal"/>
			</xsl:if>
			<xsl:if test="child::Cell[9]">
				<xsl:apply-templates select="Cell[9]" mode="normal"/>
			</xsl:if>
			<xsl:if test="child::Cell[10]">
				<xsl:apply-templates select="Cell[10]" mode="normal"/>
			</xsl:if>
			<xsl:if test="child::Cell[11]">
				<xsl:apply-templates select="Cell[11]" mode="normal"/>
			</xsl:if>
			<xsl:if test="child::Cell[12]">
				<xsl:apply-templates select="Cell[12]" mode="normal"/>
			</xsl:if>
			<xsl:if test="child::Cell[13]">
				<xsl:apply-templates select="Cell[13]" mode="normal"/>
			</xsl:if>
			<xsl:if test="child::Cell[14]">
				<xsl:apply-templates select="Cell[14]" mode="normal"/>
			</xsl:if>
		</Row>
	</xsl:template>

	<xsl:template match="Cell" mode="dateFormat">
		<Cell>
			<xsl:value-of select="."/>T00:00:00</Cell>
	</xsl:template>

	<xsl:template match="Cell" mode="normal">
		<Cell>
			<xsl:value-of select="."/>
		</Cell>
	</xsl:template>
</xsl:stylesheet><!-- Stylus Studio meta-information - (c) 2004-2007. Progress Software Corporation. All rights reserved.
<metaInformation>
<scenarios/><MapperMetaTag><MapperInfo srcSchemaPathIsRelative="yes" srcSchemaInterpretAsXML="no" destSchemaPath="" destSchemaRoot="" destSchemaPathIsRelative="yes" destSchemaInterpretAsXML="no"/><MapperBlockPosition></MapperBlockPosition><TemplateContext></TemplateContext><MapperFilter side="source"></MapperFilter></MapperMetaTag>
</metaInformation>
-->