<?xml version="1.0" encoding="utf-8"?>
<!DOCTYPE stylesheet [
<!ENTITY ø "oslash">
<!ENTITY å "aring">
<!ENTITY æ "aelig">
<!ENTITY Ø "Oslash">
<!ENTITY Å "Aring">
<!ENTITY Æ "Aelig">
]>
<!--<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">-->
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/1999/xhtml" xmlns:xhtml="http://www.w3.org/1999/xhtml" exclude-result-prefixes="xhtml">

	<!--<xsl:output method="html" version="4.01" doctype-public="http://www.w3.org/TR/html4/strict.dtd" encoding="UTF-8" indent="yes"/>-->
	<xsl:output method="xhtml" version="1.0" doctype-public="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd" encoding="UTF-8" indent="yes"/>


	<xsl:variable name="id" select="document('uniqueNames.xml')/category/group/@id"/>
	<xsl:variable name="title" select="document('settings.xml')/settings/title"/>
	<xsl:variable name="about" select="document('settings.xml')/settings/about"/>

	<xsl:template match="ingress[.!=' ']">
		<p>
			<em>
				<xsl:apply-templates/>
			</em>
		</p>
	</xsl:template>

	<xsl:template match="brodtekst/p/b">
		<em>
			<xsl:value-of select="."/>
		</em>
	</xsl:template>

	<xsl:template match="brodtekst//text()[.!=' ']">
		<xsl:choose>
			<xsl:when test="parent::brodtekst">
				<p>
					<xsl:value-of select="."/>
				</p>
			</xsl:when>
			<xsl:when test="parent::brodtekst">
				<p>
					<xsl:value-of select="."/>
				</p>
			</xsl:when>
			<xsl:when test="ancestor::p">
				<xsl:value-of select="."/>
			</xsl:when>
			<xsl:when test="parent::iord">
				<xsl:value-of select="."/>
			</xsl:when>
			<xsl:when test="parent::mel">
				<p>
					<em>
						<xsl:variable name="input" select="."/>
						<xsl:value-of select="replace($input, '\.', '')"/>
					</em>
				</p>
			</xsl:when>
			<xsl:when test="parent::b">
				<xsl:value-of select="."/>
			</xsl:when>
			<xsl:when test="parent::haf">
				<xsl:value-of select="."/>
			</xsl:when>
			<xsl:when test="parent::article">
				<xsl:value-of select="."/>
			</xsl:when>
			<xsl:otherwise>
				<p>
					<xsl:value-of select="."/>
				</p>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="brodtekst//ul">
		<ul>
			<xsl:apply-templates/>
		</ul>
	</xsl:template>

	<xsl:template match="li">
		<li>
			<xsl:value-of select="."/>
		</li>
	</xsl:template>

	<xsl:template match="*">
		<xsl:apply-templates/>
	</xsl:template>

	<xsl:template match="p[.!=' ']">
		<p>
			<xsl:apply-templates/>
		</p>
	</xsl:template>

	<xsl:template match="lead|table|@*|iframe|preform"></xsl:template>

	<xsl:template match="IMAGE">
		<span>
			<xsl:value-of select="."/>
		</span>
	</xsl:template>

	<xsl:template match="bilde">
		<p>Bildetekst:<br/><xsl:value-of select="bildetekst"/></p>
	</xsl:template>

	<xsl:template match="tittel">
		<!-- Ulik struktur på Aftenposten og E24. E24 har ett nivå ekstra. -->
		<xsl:if test="parent::innhold[1][preceding-sibling::metainformasjon/kilde eq 'aftenposten.no']">
			<h3 class="aftenposten">
				<xsl:apply-templates/>
			</h3>
		</xsl:if>
		<xsl:if test="parent::innhold[1][preceding-sibling::metainformasjon/kilde eq 'E24.no'][not(contains(preceding-sibling::metainformasjon/URL, 'kommentar'))]">
			<h4>
				<xsl:apply-templates/>
			</h4>
		</xsl:if>
		<xsl:if test="parent::innhold[1][preceding-sibling::metainformasjon/kilde eq 'E24.no'][contains(preceding-sibling::metainformasjon/URL, 'kommentar')]">
			<h3>
				<xsl:apply-templates/>
			</h3>
		</xsl:if>
		<p>
			<xsl:if test="../../metainformasjon/byline">
				<em>
					<xsl:text>Av:</xsl:text>
				</em>
				<br/>
				<xsl:for-each select="../../metainformasjon/byline/journalist">
					<xsl:value-of select="."/>
					<xsl:if test=".[@epost ne '']">
						<xsl:text>, e-post: </xsl:text>
						<xsl:value-of select="@epost"/>
					</xsl:if>.<br/></xsl:for-each>
			</xsl:if>
		</p>
	</xsl:template>

	<xsl:template match="feed">
		<xsl:param name="date-time" select="current-date()"/>
		<xsl:param name="date-time-as-string" select="string($date-time)"/>
		<xsl:param name="date" select="substring-before($date-time-as-string,'+')"/>
		<xsl:param name="year" select="substring-before($date-time-as-string,'-')"/>
		<xsl:param name="month" select="substring-before(substring-after($date,'-'),'-')"/>
		<xsl:param name="day" select="substring-after(substring-after($date,'-'),'-')"/>
		<html>
			<head>
				<title>
					<xsl:value-of select="$title"/>
					<xsl:text> </xsl:text>
					<xsl:value-of select="$day"/>.<xsl:value-of select="$month"/>.<xsl:value-of select="$year"/></title>
				<meta name="generator" content="Transformer"/>
				<meta name="description" content="Input document for Daisy production"/>
				<link href="aftenposten.css" rel="stylesheet" type="text/css" media="all"/>
			</head>
			<body>
				<!-- div-elementet under er tatt med for at SaveAsDAISY plugin'en i Word skal konvertere til DTBook feilfritt. Uten 
				dette legger den inn et tomt level1-element som gjør at DTBook-valideringen feiler. -->
				<div>
					<p>Aftenposten</p>
				</div>
				<h1 class="title">
					<xsl:value-of select="$title"/>
					<xsl:text> </xsl:text>
					<xsl:value-of select="$day"/>.<xsl:value-of select="$month"/>.<xsl:value-of select="$year"/></h1>
				<h2>Om lydavisen</h2>
				<!--<p>Lydversjonen av Aftenposten og E24 er tilgjengelig fra mandag til fredag, både på CD, som nedlastbar fil eller gjennom streaming. Lydversjonen av Aftenposten og E24 er basert på utdrag fra en database, og har derfor ikke samme innhold og struktur som originalutgaven av avisen. Avisen har nå 20 hovedkategorier. Disse er: meninger, innenriks, utenriks, kultur, sport, sportresultater, fakta, si-d, kongelige, miljø, økonomi, oslo, nett og it, dyr, vær, mat og vin, helse, forbruker, a-magasinet og natur og vitenskap. Vær oppmerksom på at hovedkategorier enkelte dager kan være utelatt fra lydversjonen av avisen. Dette forekommer på dager der det ikke finnes noen artikler som tilhører den aktuelle hovedkategorien. Kontakt oss, fortrinnsvis på: lydavis@nlb.no dersom du har spørsmål om lydavisen.</p>-->
				<p>
					<xsl:value-of select="$about"/>
				</p>
				<h1>Aftenposten</h1>
				<xsl:variable name="currentUniqueName1" select="document('uniqueNames.xml')/category/group[1]/uniqueName"/>
				<xsl:if test="$currentUniqueName1">
					<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName1]">
						<h2>
							<xsl:value-of select="$id[1]"/>
						</h2>
						<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName1]">
							<div class="article">
								<xsl:apply-templates select="../../innhold"/>
							</div>
						</xsl:for-each>
					</xsl:if>
				</xsl:if>
				<xsl:variable name="currentUniqueName2" select="document('uniqueNames.xml')/category/group[2]/uniqueName"/>
				<xsl:if test="$currentUniqueName2">
					<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName2]">
						<h2>
							<xsl:value-of select="$id[2]"/>
						</h2>
						<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName2]">
							<div class="article">
								<xsl:apply-templates select="../../innhold"/>
							</div>
						</xsl:for-each>
					</xsl:if>
				</xsl:if>

				<xsl:variable name="currentUniqueName3" select="document('uniqueNames.xml')/category/group[3]/uniqueName"/>
				<xsl:if test="$currentUniqueName3">
					<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName3]">
						<h2>
							<xsl:value-of select="$id[3]"/>
						</h2>
						<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName3]">
							<div class="article">
								<xsl:apply-templates select="../../innhold"/>
							</div>
						</xsl:for-each>
					</xsl:if>
				</xsl:if>

				<xsl:variable name="currentUniqueName4" select="document('uniqueNames.xml')/category/group[4]/uniqueName"/>
				<xsl:if test="$currentUniqueName4">
					<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName4]">
						<h2>
							<xsl:value-of select="$id[4]"/>
						</h2>
						<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName4]">
							<div class="article">
								<xsl:apply-templates select="../../innhold"/>
							</div>
						</xsl:for-each>
					</xsl:if>
				</xsl:if>

				<xsl:variable name="currentUniqueName5" select="document('uniqueNames.xml')/category/group[5]/uniqueName"/>
				<xsl:if test="$currentUniqueName5">
					<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName5]">
						<h2>
							<xsl:value-of select="$id[5]"/>
						</h2>
						<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName5]">
							<div class="article">
								<xsl:apply-templates select="../../innhold"/>
							</div>
						</xsl:for-each>
					</xsl:if>
				</xsl:if>

				<xsl:variable name="currentUniqueName6" select="document('uniqueNames.xml')/category/group[6]/uniqueName"/>
				<xsl:if test="$currentUniqueName6">
					<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName6]">
						<h2>
							<xsl:value-of select="$id[6]"/>
						</h2>
						<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName6]">
							<div class="article">
								<xsl:apply-templates select="../../innhold"/>
							</div>
						</xsl:for-each>
					</xsl:if>
				</xsl:if>

				<xsl:variable name="currentUniqueName7" select="document('uniqueNames.xml')/category/group[7]/uniqueName"/>
				<xsl:if test="$currentUniqueName7">
					<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName7]">
						<h2>
							<xsl:value-of select="$id[7]"/>
						</h2>
						<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName7]">
							<div class="article">
								<xsl:apply-templates select="../../innhold"/>
							</div>
						</xsl:for-each>
					</xsl:if>
				</xsl:if>

				<xsl:variable name="currentUniqueName8" select="document('uniqueNames.xml')/category/group[8]/uniqueName"/>
				<xsl:if test="$currentUniqueName8">
					<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName8]">
						<h2>
							<xsl:value-of select="$id[8]"/>
						</h2>
						<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName8]">
							<div class="article">
								<xsl:apply-templates select="../../innhold"/>
							</div>
						</xsl:for-each>
					</xsl:if>
				</xsl:if>

				<xsl:variable name="currentUniqueName9" select="document('uniqueNames.xml')/category/group[9]/uniqueName"/>
				<xsl:if test="$currentUniqueName9">
					<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName9]">
						<h2>
							<xsl:value-of select="$id[9]"/>
						</h2>
						<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName9]">
							<div class="article">
								<xsl:apply-templates select="../../innhold"/>
							</div>
						</xsl:for-each>
					</xsl:if>
				</xsl:if>

				<xsl:variable name="currentUniqueName10" select="document('uniqueNames.xml')/category/group[10]/uniqueName"/>
				<xsl:if test="$currentUniqueName10">
					<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName10]">
						<h2>
							<xsl:value-of select="$id[10]"/>
						</h2>
						<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName10]">
							<div class="article">
								<xsl:apply-templates select="../../innhold"/>
							</div>
						</xsl:for-each>
					</xsl:if>
				</xsl:if>

				<xsl:variable name="currentUniqueName11" select="document('uniqueNames.xml')/category/group[11]/uniqueName"/>
				<xsl:if test="$currentUniqueName11">
					<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName11]">
						<h2>Miljø</h2>
						<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName11]">
							<div class="article">
								<xsl:apply-templates select="../../innhold"/>
							</div>
						</xsl:for-each>
					</xsl:if>
				</xsl:if>

				<xsl:variable name="currentUniqueName12" select="document('uniqueNames.xml')/category/group[12]/uniqueName"/>
				<xsl:if test="$currentUniqueName12">
					<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName12]">
						<h2>Økonomi</h2>
						<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName12]">
							<div class="article">
								<xsl:apply-templates select="../../innhold"/>
							</div>
						</xsl:for-each>
					</xsl:if>
				</xsl:if>

				<xsl:variable name="currentUniqueName13" select="document('uniqueNames.xml')/category/group[13]/uniqueName"/>
				<xsl:if test="$currentUniqueName13">
					<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName13]">
						<h2>Vær</h2>
						<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName13]">
							<div class="article">
								<xsl:apply-templates select="../../innhold"/>
							</div>
						</xsl:for-each>
					</xsl:if>
				</xsl:if>

				<xsl:variable name="currentUniqueName14" select="document('uniqueNames.xml')/category/group[14]/uniqueName"/>
				<xsl:if test="$currentUniqueName14">
					<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName14]">
						<h2>
							<xsl:value-of select="$id[14]"/>
						</h2>
						<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName14]">
							<div class="article">
								<xsl:apply-templates select="../../innhold"/>
							</div>
						</xsl:for-each>
					</xsl:if>
				</xsl:if>

				<xsl:variable name="currentUniqueName15" select="document('uniqueNames.xml')/category/group[15]/uniqueName"/>
				<xsl:if test="$currentUniqueName15">
					<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName15]">
						<h2>
							<xsl:value-of select="$id[15]"/>
						</h2>
						<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName15]">
							<div class="article">
								<xsl:apply-templates select="../../innhold"/>
							</div>
						</xsl:for-each>
					</xsl:if>
				</xsl:if>

				<xsl:variable name="currentUniqueName16" select="document('uniqueNames.xml')/category/group[16]/uniqueName"/>
				<xsl:if test="$currentUniqueName16">
					<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName16]">
						<h2>
							<xsl:value-of select="$id[16]"/>
						</h2>
						<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName16]">
							<div class="article">
								<xsl:apply-templates select="../../innhold"/>
							</div>
						</xsl:for-each>
					</xsl:if>
				</xsl:if>

				<xsl:variable name="currentUniqueName17" select="document('uniqueNames.xml')/category/group[17]/uniqueName"/>
				<xsl:if test="$currentUniqueName17">
					<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName17]">
						<h2>
							<xsl:value-of select="$id[17]"/>
						</h2>
						<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName17]">
							<div class="article">
								<xsl:apply-templates select="../../innhold"/>
							</div>
						</xsl:for-each>
					</xsl:if>
				</xsl:if>

				<xsl:variable name="currentUniqueName18" select="document('uniqueNames.xml')/category/group[18]/uniqueName"/>
				<xsl:if test="$currentUniqueName18">
					<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName18]">
						<h2>
							<xsl:value-of select="$id[18]"/>
						</h2>
						<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName18]">
							<div class="article">
								<xsl:apply-templates select="../../innhold"/>
							</div>
						</xsl:for-each>
					</xsl:if>
				</xsl:if>

				<xsl:variable name="currentUniqueName19" select="document('uniqueNames.xml')/category/group[19]/uniqueName"/>
				<xsl:if test="$currentUniqueName19">
					<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName19]">
						<h2>
							<xsl:value-of select="$id[19]"/>
						</h2>
						<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName19]">
							<div class="article">
								<xsl:apply-templates select="../../innhold"/>
							</div>
						</xsl:for-each>
					</xsl:if>
				</xsl:if>

				<xsl:variable name="currentUniqueName20" select="document('uniqueNames.xml')/category/group[20]/uniqueName"/>
				<xsl:if test="$currentUniqueName20">
					<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName20]">
						<h2>
							<xsl:value-of select="$id[20]"/>
						</h2>
						<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName20]">
							<div class="article">
								<xsl:apply-templates select="../../innhold"/>
							</div>
						</xsl:for-each>
					</xsl:if>
				</xsl:if>

				<h1>E24</h1>

				<h2>Nyheter</h2>
				<p>Økonominyheter fra hele verden.</p>

				<xsl:variable name="currentUniqueName21" select="document('uniqueNames.xml')/category/group[21]/uniqueName"/>
				<xsl:if test="$currentUniqueName21">
					<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName21]">
						<h3 class="e24">Børs &amp; Finans</h3>
						<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName21]">
							<div class="article">
								<xsl:apply-templates select="../../innhold"/>
							</div>
						</xsl:for-each>
					</xsl:if>
				</xsl:if>
				<xsl:variable name="currentUniqueName22" select="document('uniqueNames.xml')/category/group[22]/uniqueName"/>
				<xsl:if test="$currentUniqueName22">
					<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName22]">
						<h3 class="e24">Kvartalsresultater</h3>
						<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName22]">
							<div class="article">
								<xsl:apply-templates select="../../innhold"/>
							</div>
						</xsl:for-each>
					</xsl:if>
				</xsl:if>
				<xsl:variable name="currentUniqueName23" select="document('uniqueNames.xml')/category/group[23]/uniqueName"/>
				<xsl:if test="$currentUniqueName23">
					<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName23]">
						<h3 class="e24">Makro &amp; Politikk</h3>
						<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName23]">
							<div class="article">
								<xsl:apply-templates select="../../innhold"/>
							</div>
						</xsl:for-each>
					</xsl:if>
				</xsl:if>
				<xsl:variable name="currentUniqueName24" select="document('uniqueNames.xml')/category/group[24]/uniqueName"/>
				<xsl:if test="$currentUniqueName24">
					<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName24]">
						<h3 class="e24">Næringsliv</h3>
						<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName24]">
							<div class="article">
								<xsl:apply-templates select="../../innhold"/>
							</div>
						</xsl:for-each>
					</xsl:if>
				</xsl:if>
				<xsl:variable name="currentUniqueName25" select="document('uniqueNames.xml')/category/group[25]/uniqueName"/>
				<xsl:if test="$currentUniqueName25">
					<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName25]">
						<h3 class="e24">Utenriks</h3>
						<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName25]">
							<div class="article">
								<xsl:apply-templates select="../../innhold"/>
							</div>
						</xsl:for-each>
					</xsl:if>
				</xsl:if>
				<xsl:variable name="currentUniqueName26" select="document('uniqueNames.xml')/category/group[26]/uniqueName"/>
				<xsl:if test="$currentUniqueName26">
					<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName26]">
						<h3 class="e24">Lov og Rett</h3>
						<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName26]">
							<div class="article">
								<xsl:apply-templates select="../../innhold"/>
							</div>
						</xsl:for-each>
					</xsl:if>
				</xsl:if>
				<xsl:variable name="currentUniqueName27" select="document('uniqueNames.xml')/category/group[27]/uniqueName"/>
				<xsl:if test="$currentUniqueName27">
					<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName27]">
						<h3 class="e24">Eiendom</h3>
						<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName27]">
							<div class="article">
								<xsl:apply-templates select="../../innhold"/>
							</div>
						</xsl:for-each>
					</xsl:if>
				</xsl:if>
				<xsl:variable name="currentUniqueName28" select="document('uniqueNames.xml')/category/group[28]/uniqueName"/>
				<xsl:if test="$currentUniqueName28">
					<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName28]">
						<h3 class="e24">Medier og Reklame</h3>
						<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName28]">
							<div class="article">
								<xsl:apply-templates select="../../innhold"/>
							</div>
						</xsl:for-each>
					</xsl:if>
				</xsl:if>
				<xsl:variable name="currentUniqueName29" select="document('uniqueNames.xml')/category/group[29]/uniqueName"/>
				<xsl:if test="$currentUniqueName29">
					<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName29]">
						<h3 class="e24">Eksklusiv livsstil</h3>
						<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName29]">
							<div class="article">
								<xsl:apply-templates select="../../innhold"/>
							</div>
						</xsl:for-each>
					</xsl:if>
				</xsl:if>

				<h2>Børs</h2>
				<p>Børsnyheter fra hele verden.</p>

				<xsl:variable name="currentUniqueName30" select="document('uniqueNames.xml')/category/group[30]/uniqueName"/>
				<xsl:if test="$currentUniqueName30">
					<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName30]">
						<h3 class="e24">Oslo børs</h3>
						<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName30]">
							<div class="article">
								<xsl:apply-templates select="../../innhold"/>
							</div>
						</xsl:for-each>
					</xsl:if>
				</xsl:if>
				<xsl:variable name="currentUniqueName31" select="document('uniqueNames.xml')/category/group[31]/uniqueName"/>
				<xsl:if test="$currentUniqueName31">
					<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName31]">
						<h3 class="e24">Europeiske børser</h3>
						<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName31]">
							<div class="article">
								<xsl:apply-templates select="../../innhold"/>
							</div>
						</xsl:for-each>
					</xsl:if>
				</xsl:if>
				<xsl:variable name="currentUniqueName32" select="document('uniqueNames.xml')/category/group[32]/uniqueName"/>
				<xsl:if test="$currentUniqueName32">
					<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName32]">
						<h3 class="e24">Verdens børser</h3>
						<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName32]">
							<div class="article">
								<xsl:apply-templates select="../../innhold"/>
							</div>
						</xsl:for-each>
					</xsl:if>
				</xsl:if>
				<xsl:variable name="currentUniqueName33" select="document('uniqueNames.xml')/category/group[33]/uniqueName"/>
				<xsl:if test="$currentUniqueName33">
					<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName33]">
						<h3 class="e24">Aksjer</h3>
						<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName33]">
							<div class="article">
								<xsl:apply-templates select="../../innhold"/>
							</div>
						</xsl:for-each>
					</xsl:if>
				</xsl:if>
				<xsl:variable name="currentUniqueName34" select="document('uniqueNames.xml')/category/group[34]/uniqueName"/>
				<xsl:if test="$currentUniqueName34">
					<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName34]">
						<h3 class="e24">Fond</h3>
						<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName34]">
							<div class="article">
								<xsl:apply-templates select="../../innhold"/>
							</div>
						</xsl:for-each>
					</xsl:if>
				</xsl:if>
				<xsl:variable name="currentUniqueName35" select="document('uniqueNames.xml')/category/group[35]/uniqueName"/>
				<xsl:if test="$currentUniqueName35">
					<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName35]">
						<h3 class="e24">Valuta</h3>
						<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName35]">
							<div class="article">
								<xsl:apply-templates select="../../innhold"/>
							</div>
						</xsl:for-each>
					</xsl:if>
				</xsl:if>
				<xsl:variable name="currentUniqueName36" select="document('uniqueNames.xml')/category/group[36]/uniqueName"/>
				<xsl:if test="$currentUniqueName36">
					<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName36]">
						<h3 class="e24">Renter</h3>
						<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName36]">
							<div class="article">
								<xsl:apply-templates select="../../innhold"/>
							</div>
						</xsl:for-each>
					</xsl:if>
				</xsl:if>
				<xsl:variable name="currentUniqueName37" select="document('uniqueNames.xml')/category/group[37]/uniqueName"/>
				<xsl:if test="$currentUniqueName37">
					<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName37]">
						<h3 class="e24">Olje og andre råvarer</h3>
						<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName37]">
							<div class="article">
								<xsl:apply-templates select="../../innhold"/>
							</div>
						</xsl:for-each>
					</xsl:if>
				</xsl:if>
				<xsl:variable name="currentUniqueName38" select="document('uniqueNames.xml')/category/group[38]/uniqueName"/>
				<xsl:if test="$currentUniqueName38">
					<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName38]">
						<h3 class="e24">Indexer</h3>
						<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName38]">
							<div class="article">
								<xsl:apply-templates select="../../innhold"/>
							</div>
						</xsl:for-each>
					</xsl:if>
				</xsl:if>
				<xsl:variable name="currentUniqueName39" select="document('uniqueNames.xml')/category/group[39]/uniqueName"/>
				<xsl:if test="$currentUniqueName39">
					<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName39]">
						<h3 class="e24">Derivater</h3>
						<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName39]">
							<div class="article">
								<xsl:apply-templates select="../../innhold"/>
							</div>
						</xsl:for-each>
					</xsl:if>
				</xsl:if>

				<xsl:variable name="currentUniqueName40" select="document('uniqueNames.xml')/category/group[40]/uniqueName"/>
				<xsl:if test="$currentUniqueName40">
					<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName40]">
						<h2>Kommentar</h2>
						<p>Kommentarer fra E24.</p>
						<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName40]">
							<div class="article">
								<xsl:apply-templates select="../../innhold"/>
							</div>
						</xsl:for-each>
					</xsl:if>
				</xsl:if>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet><!-- Stylus Studio meta-information - (c) 2004-2007. Progress Software Corporation. All rights reserved.
<metaInformation>
<scenarios/><MapperMetaTag><MapperInfo srcSchemaPathIsRelative="yes" srcSchemaInterpretAsXML="no" destSchemaPath="" destSchemaRoot="" destSchemaPathIsRelative="yes" destSchemaInterpretAsXML="no"/><MapperBlockPosition></MapperBlockPosition><TemplateContext></TemplateContext><MapperFilter side="source"></MapperFilter></MapperMetaTag>
</metaInformation>
-->