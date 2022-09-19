<?xml version="1.0" encoding="utf-8"?>
<!DOCTYPE stylesheet [
<!ENTITY ø "oslash">
<!ENTITY å "aring">
<!ENTITY æ "aelig">
]>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:dtbook="http://www.daisy.org/z3986/2005/dtbook/" xmlns="http://www.daisy.org/z3986/2005/dtbook/" exclude-result-prefixes="dtbook">

	<xsl:output method="xml" encoding="ISO-8859-1" indent="yes" version="1.0" doctype-public="-//NISO//DTD dtbook 2005-3//EN" doctype-system="http://www.daisy.org/z3986/2005/dtbook-2005-3.dtd"/>

	<!-- Varible hentet fra andre filer. -->
	<!-- Tittel på avisen er nå en fast tekst i stedet for denne variabelen. -->
	<!--<xsl:variable name="title" select="document('/settings.xml')/settings/title"/>-->
	<!-- About inneholder beskrivelsen av Aftenposten. Denne kan redigeres i programmet. -->
	<xsl:variable name="about" select="document('settings.xml')/settings/about"/>
	<!-- Henter path for uniqueNames.xml. uniqueNames.xml kan ligge på ulike steder avhengig av hva som ble valgt under installasjon. -->
	<xsl:variable name="uniqueNamesPath" select="document('settings.xml')/settings/uniqueNamesPath"/>
	<!-- @id inneholder navn på de ulike kategoriene i Aftenposten/E24. -->
	<!--<xsl:variable name="id" select="document('/uniqueNames.xml', $uniqueNamesPath)/category/group/@id"/>-->
	<!--<xsl:variable name="id" select="document('file:///C:/UniqueNames/uniqueNames.xml')/category/group/@id"/>-->
	<xsl:variable name="id" select="document(concat($uniqueNamesPath, 'uniqueNames.xml'))/category/group/@id"/>


	<xsl:strip-space elements="*"/>

	<xsl:template match="ingress[.!=' ']">
		<p>
			<em>
				<xsl:apply-templates/>
			</em>
		</p>
	</xsl:template>

	<xsl:template match="p[.!=' ']">
		<p>
			<xsl:value-of select="."/>
		</p>
	</xsl:template>

	<xsl:template match="brodtekst//text()[.!=' ']">
		<xsl:choose>
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
			<xsl:otherwise>
				<p>
					<xsl:value-of select="."/>
				</p>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="*">
		<xsl:apply-templates/>
	</xsl:template>

	<xsl:template match="lead|table|@*|iframe|preform"></xsl:template>

	<xsl:template match="IMAGE">
		<imggroup>
			<caption>
				<xsl:value-of select="."/>
			</caption>
		</imggroup>
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
		<!--<dtbook version="2005-3">-->
		<dtbook xmlns="http://www.daisy.org/z3986/2005/dtbook/" version="2005-3" xml:lang="NO">
			<head>
				<meta name="dtb:uid" content="no-00001"/>
				<meta name="dc:Publisher" content="NLB"/>
				<meta name="dc:Title" content="Aftenposten"/>
				<meta name="dc:Language" content="nb-NO"/>
				<meta name="generator" content="Transformer"/>
				<meta name="description" content="Input document for Daisy production"/>
				<link href="aftenposten.css" rel="stylesheet" type="text/css" media="all"/>
			</head>
			<book>
				<frontmatter>
					<doctitle>
						<!--<xsl:value-of select="$title"/>-->
						<xsl:text>Aftenposten </xsl:text>
						<xsl:value-of select="$day"/>.<xsl:value-of select="$month"/>.<xsl:value-of select="$year"/></doctitle>
					<level1 class="preface">
						<h1>Om lydavisen</h1>
						<p>Lydversjonen av Aftenposten er tilgjengelig fra mandag til fredag, både på CD, som nedlastbar fil eller gjennom streaming. Lydversjonen av Aftenposten er basert på utdrag fra en database, og har derfor ikke samme innhold og struktur som originalutgaven av avisen. Avisen har nå 20 hovedkategorier. Disse er: meninger, innenriks, utenriks, kultur, sport, sportresultater, fakta, si-d, kongelige, miljø, økonomi, oslo, nett og it, dyr, vær, mat og vin, helse, forbruker, a-magasinet og natur og vitenskap. Vær oppmerksom på at hovedkategorier enkelte dager kan være utelatt fra lydversjonen av avisen. Dette forekommer på dager der det ikke finnes noen artikler som tilhører den aktuelle hovedkategorien. Kontakt oss, fortrinnsvis på: lydavis@nlb.no dersom du har spørsmål om lydavisen.</p>
					</level1>
				</frontmatter>
				<bodymatter>
					<!--<level1>
						<doctitle>
							<xsl:value-of select="$title"/>
							<xsl:text> </xsl:text>
							<xsl:value-of select="$day"/>.<xsl:value-of select="$month"/>.<xsl:value-of select="$year"/></doctitle>
					</level1>
					<level1>
						<h1 class="title">
							<xsl:value-of select="$title"/>
							<xsl:text> </xsl:text>
							<xsl:value-of select="$day"/>.<xsl:value-of select="$month"/>.<xsl:value-of select="$year"/></h1>
						<level2>
							<h2>Om lydavisen</h2>
							<p>
								<xsl:value-of select="$about"/>
							</p>
						</level2>
					</level1>-->
					<level1>
						<h1>Aftenposten</h1>
						<!--<xsl:variable name="currentUniqueName1" select="document('/uniqueNames.xml', $uniqueNamesPath)/category/group[1]/uniqueName"/>-->
						<xsl:variable name="currentUniqueName1" select="document(concat($uniqueNamesPath, 'uniqueNames.xml'))/category/group[1]/uniqueName"/>
						<xsl:if test="$currentUniqueName1">
							<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName1]">
								<level2>
									<h2>
										<xsl:value-of select="$id[1]"/>
									</h2>
									<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName1]">
										<level3>
											<xsl:apply-templates select="../../innhold"/>
										</level3>
									</xsl:for-each>
								</level2>
							</xsl:if>
						</xsl:if>
						<xsl:variable name="currentUniqueName2" select="document(concat($uniqueNamesPath, 'uniqueNames.xml'))/category/group[2]/uniqueName"/>
						<xsl:if test="$currentUniqueName2">
							<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName2]">
								<level2>
									<h2>
										<xsl:value-of select="$id[2]"/>
									</h2>
									<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName2]">
										<level3>
											<xsl:apply-templates select="../../innhold"/>
										</level3>
									</xsl:for-each>
								</level2>
							</xsl:if>
						</xsl:if>
						<xsl:variable name="currentUniqueName3" select="document(concat($uniqueNamesPath, 'uniqueNames.xml'))/category/group[3]/uniqueName"/>
						<xsl:if test="$currentUniqueName3">
							<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName3]">
								<level2>
									<h2>
										<xsl:value-of select="$id[3]"/>
									</h2>
									<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName3]">
										<level3>
											<xsl:apply-templates select="../../innhold"/>
										</level3>
									</xsl:for-each>
								</level2>
							</xsl:if>
						</xsl:if>
						<xsl:variable name="currentUniqueName4" select="document(concat($uniqueNamesPath, 'uniqueNames.xml'))/category/group[4]/uniqueName"/>
						<xsl:if test="$currentUniqueName4">
							<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName4]">
								<level2>
									<h2>
										<xsl:value-of select="$id[4]"/>
									</h2>
									<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName4]">
										<level3>
											<xsl:apply-templates select="../../innhold"/>
										</level3>
									</xsl:for-each>
								</level2>
							</xsl:if>
						</xsl:if>

						<xsl:variable name="currentUniqueName5" select="document(concat($uniqueNamesPath, 'uniqueNames.xml'))/category/group[5]/uniqueName"/>
						<xsl:if test="$currentUniqueName5">
							<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName5]">
								<level2>
									<h2>
										<xsl:value-of select="$id[5]"/>
									</h2>
									<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName5]">
										<level3>
											<xsl:apply-templates select="../../innhold"/>
										</level3>
									</xsl:for-each>
								</level2>
							</xsl:if>
						</xsl:if>
						<xsl:variable name="currentUniqueName6" select="document(concat($uniqueNamesPath, 'uniqueNames.xml'))/category/group[6]/uniqueName"/>
						<xsl:if test="$currentUniqueName6">
							<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName6]">
								<level2>
									<h2>
										<xsl:value-of select="$id[6]"/>
									</h2>
									<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName6]">
										<level3>
											<xsl:apply-templates select="../../innhold"/>
										</level3>
									</xsl:for-each>
								</level2>
							</xsl:if>
						</xsl:if>
						<xsl:variable name="currentUniqueName7" select="document(concat($uniqueNamesPath, 'uniqueNames.xml'))/category/group[7]/uniqueName"/>
						<xsl:if test="$currentUniqueName7">
							<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName7]">
								<level2>
									<h2>
										<xsl:value-of select="$id[7]"/>
									</h2>
									<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName7]">
										<level3>
											<xsl:apply-templates select="../../innhold"/>
										</level3>
									</xsl:for-each>
								</level2>
							</xsl:if>
						</xsl:if>
						<xsl:variable name="currentUniqueName8" select="document(concat($uniqueNamesPath, 'uniqueNames.xml'))/category/group[8]/uniqueName"/>
						<xsl:if test="$currentUniqueName8">
							<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName8]">
								<level2>
									<h2>
										<xsl:value-of select="$id[8]"/>
									</h2>
									<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName8]">
										<level3>
											<xsl:apply-templates select="../../innhold"/>
										</level3>
									</xsl:for-each>
								</level2>
							</xsl:if>
						</xsl:if>
						<xsl:variable name="currentUniqueName9" select="document(concat($uniqueNamesPath, 'uniqueNames.xml'))/category/group[9]/uniqueName"/>
						<xsl:if test="$currentUniqueName9">
							<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName9]">
								<level2>
									<h2>
										<xsl:value-of select="$id[9]"/>
									</h2>
									<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName9]">
										<level3>
											<xsl:apply-templates select="../../innhold"/>
										</level3>
									</xsl:for-each>
								</level2>
							</xsl:if>
						</xsl:if>
						<xsl:variable name="currentUniqueName10" select="document(concat($uniqueNamesPath, 'uniqueNames.xml'))/category/group[10]/uniqueName"/>
						<xsl:if test="$currentUniqueName10">
							<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName10]">
								<level2>
									<h2>
										<xsl:value-of select="$id[10]"/>
									</h2>
									<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName10]">
										<level3>
											<xsl:apply-templates select="../../innhold"/>
										</level3>
									</xsl:for-each>
								</level2>
							</xsl:if>
						</xsl:if>
						<xsl:variable name="currentUniqueName11" select="document(concat($uniqueNamesPath, 'uniqueNames.xml'))/category/group[11]/uniqueName"/>
						<xsl:if test="$currentUniqueName11">
							<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName11]">
								<level2>
									<h2>
										<xsl:value-of select="$id[11]"/>
									</h2>
									<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName11]">
										<level3>
											<xsl:apply-templates select="../../innhold"/>
										</level3>
									</xsl:for-each>
								</level2>
							</xsl:if>
						</xsl:if>
						<xsl:variable name="currentUniqueName12" select="document(concat($uniqueNamesPath, 'uniqueNames.xml'))/category/group[12]/uniqueName"/>
						<xsl:if test="$currentUniqueName12">
							<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName12]">
								<level2>
									<h2>
										<xsl:value-of select="$id[12]"/>
									</h2>
									<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName12]">
										<level3>
											<xsl:apply-templates select="../../innhold"/>
										</level3>
									</xsl:for-each>
								</level2>
							</xsl:if>
						</xsl:if>
						<xsl:variable name="currentUniqueName13" select="document(concat($uniqueNamesPath, 'uniqueNames.xml'))/category/group[13]/uniqueName"/>
						<xsl:if test="$currentUniqueName13">
							<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName13]">
								<level2>
									<h2>
										<xsl:value-of select="$id[13]"/>
									</h2>
									<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName13]">
										<level3>
											<xsl:apply-templates select="../../innhold"/>
										</level3>
									</xsl:for-each>
								</level2>
							</xsl:if>
						</xsl:if>
						<xsl:variable name="currentUniqueName14" select="document(concat($uniqueNamesPath, 'uniqueNames.xml'))/category/group[14]/uniqueName"/>
						<xsl:if test="$currentUniqueName14">
							<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName14]">
								<level2>
									<h2>
										<xsl:value-of select="$id[14]"/>
									</h2>
									<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName14]">
										<level3>
											<xsl:apply-templates select="../../innhold"/>
										</level3>
									</xsl:for-each>
								</level2>
							</xsl:if>
						</xsl:if>
						<xsl:variable name="currentUniqueName15" select="document(concat($uniqueNamesPath, 'uniqueNames.xml'))/category/group[15]/uniqueName"/>
						<xsl:if test="$currentUniqueName15">
							<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName15]">
								<level2>
									<h2>
										<xsl:value-of select="$id[15]"/>
									</h2>
									<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName15]">
										<level3>
											<xsl:apply-templates select="../../innhold"/>
										</level3>
									</xsl:for-each>
								</level2>
							</xsl:if>
						</xsl:if>
						<xsl:variable name="currentUniqueName16" select="document(concat($uniqueNamesPath, 'uniqueNames.xml'))/category/group[16]/uniqueName"/>
						<xsl:if test="$currentUniqueName16">
							<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName16]">
								<level2>
									<h2>
										<xsl:value-of select="$id[16]"/>
									</h2>
									<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName16]">
										<level3>
											<xsl:apply-templates select="../../innhold"/>
										</level3>
									</xsl:for-each>
								</level2>
							</xsl:if>
						</xsl:if>
						<xsl:variable name="currentUniqueName17" select="document(concat($uniqueNamesPath, 'uniqueNames.xml'))/category/group[17]/uniqueName"/>
						<xsl:if test="$currentUniqueName17">
							<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName17]">
								<level2>
									<h2>
										<xsl:value-of select="$id[17]"/>
									</h2>
									<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName17]">
										<level3>
											<xsl:apply-templates select="../../innhold"/>
										</level3>
									</xsl:for-each>
								</level2>
							</xsl:if>
						</xsl:if>
						<xsl:variable name="currentUniqueName18" select="document(concat($uniqueNamesPath, 'uniqueNames.xml'))/category/group[18]/uniqueName"/>
						<xsl:if test="$currentUniqueName18">
							<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName18]">
								<level2>
									<h2>
										<xsl:value-of select="$id[18]"/>
									</h2>
									<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName18]">
										<level3>
											<xsl:apply-templates select="../../innhold"/>
										</level3>
									</xsl:for-each>
								</level2>
							</xsl:if>
						</xsl:if>
						<xsl:variable name="currentUniqueName19" select="document(concat($uniqueNamesPath, 'uniqueNames.xml'))/category/group[19]/uniqueName"/>
						<xsl:if test="$currentUniqueName19">
							<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName19]">
								<level2>
									<h2>
										<xsl:value-of select="$id[19]"/>
									</h2>
									<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName19]">
										<level3>
											<xsl:apply-templates select="../../innhold"/>
										</level3>
									</xsl:for-each>
								</level2>
							</xsl:if>
						</xsl:if>
						<xsl:variable name="currentUniqueName20" select="document(concat($uniqueNamesPath, 'uniqueNames.xml'))/category/group[20]/uniqueName"/>
						<xsl:if test="$currentUniqueName20">
							<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName20]">
								<level2>
									<h2>
										<xsl:value-of select="$id[20]"/>
									</h2>
									<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName20]">
										<level3>
											<xsl:apply-templates select="../../innhold"/>
										</level3>
									</xsl:for-each>
								</level2>
							</xsl:if>
						</xsl:if>
					</level1>
					<level1>
						<h1>E24</h1>
						<level2>
							<h2>Nyheter</h2>
							<p>Økonominyheter fra hele verden.</p>
							<xsl:variable name="currentUniqueName21" select="document(concat($uniqueNamesPath, 'uniqueNames.xml'))/category/group[21]/uniqueName"/>
							<xsl:if test="$currentUniqueName21">
								<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName21]">
									<level3>
										<h3 class="e24">Børs &amp; Finans</h3>
										<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName21]">
											<level4>
												<xsl:apply-templates select="../../innhold"/>
											</level4>
										</xsl:for-each>
									</level3>
								</xsl:if>
							</xsl:if>
							<xsl:variable name="currentUniqueName22" select="document(concat($uniqueNamesPath, 'uniqueNames.xml'))/category/group[22]/uniqueName"/>
							<xsl:if test="$currentUniqueName22">
								<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName22]">
									<level3>
										<h3 class="e24">Kvartalsresultater</h3>
										<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName22]">
											<level4>
												<xsl:apply-templates select="../../innhold"/>
											</level4>
										</xsl:for-each>
									</level3>
								</xsl:if>
							</xsl:if>
							<xsl:variable name="currentUniqueName23" select="document(concat($uniqueNamesPath, 'uniqueNames.xml'))/category/group[23]/uniqueName"/>
							<xsl:if test="$currentUniqueName23">
								<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName23]">
									<level3>
										<h3 class="e24">Makro &amp; Politikk</h3>
										<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName23]">
											<level4>
												<xsl:apply-templates select="../../innhold"/>
											</level4>
										</xsl:for-each>
									</level3>
								</xsl:if>
							</xsl:if>
							<xsl:variable name="currentUniqueName24" select="document(concat($uniqueNamesPath, 'uniqueNames.xml'))/category/group[24]/uniqueName"/>
							<xsl:if test="$currentUniqueName24">
								<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName24]">
									<level3>
										<h3 class="e24">Næringsliv</h3>
										<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName24]">
											<level4>
												<xsl:apply-templates select="../../innhold"/>
											</level4>
										</xsl:for-each>
									</level3>
								</xsl:if>
							</xsl:if>
							<xsl:variable name="currentUniqueName25" select="document(concat($uniqueNamesPath, 'uniqueNames.xml'))/category/group[25]/uniqueName"/>
							<xsl:if test="$currentUniqueName25">
								<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName25]">
									<level3>
										<h3 class="e24">Utenriks</h3>
										<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName25]">
											<level4>
												<xsl:apply-templates select="../../innhold"/>
											</level4>
										</xsl:for-each>
									</level3>
								</xsl:if>
							</xsl:if>
							<xsl:variable name="currentUniqueName26" select="document(concat($uniqueNamesPath, 'uniqueNames.xml'))/category/group[26]/uniqueName"/>
							<xsl:if test="$currentUniqueName26">
								<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName26]">
									<level3>
										<h3 class="e24">Lov &amp; Rett</h3>
										<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName26]">
											<level4>
												<xsl:apply-templates select="../../innhold"/>
											</level4>
										</xsl:for-each>
									</level3>
								</xsl:if>
							</xsl:if>
							<xsl:variable name="currentUniqueName27" select="document(concat($uniqueNamesPath, 'uniqueNames.xml'))/category/group[27]/uniqueName"/>
							<xsl:if test="$currentUniqueName27">
								<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName27]">
									<level3>
										<h3 class="e24">Eiendom</h3>
										<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName27]">
											<level4>
												<xsl:apply-templates select="../../innhold"/>
											</level4>
										</xsl:for-each>
									</level3>
								</xsl:if>
							</xsl:if>
							<xsl:variable name="currentUniqueName28" select="document(concat($uniqueNamesPath, 'uniqueNames.xml'))/category/group[28]/uniqueName"/>
							<xsl:if test="$currentUniqueName28">
								<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName28]">
									<level3>
										<h3 class="e24">Medier &amp; Reklame</h3>
										<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName28]">
											<level4>
												<xsl:apply-templates select="../../innhold"/>
											</level4>
										</xsl:for-each>
									</level3>
								</xsl:if>
							</xsl:if>
							<xsl:variable name="currentUniqueName29" select="document(concat($uniqueNamesPath, 'uniqueNames.xml'))/category/group[29]/uniqueName"/>
							<xsl:if test="$currentUniqueName29">
								<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName29]">
									<level3>
										<h3 class="e24">Eksklusiv livsstil</h3>
										<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName29]">
											<level4>
												<xsl:apply-templates select="../../innhold"/>
											</level4>
										</xsl:for-each>
									</level3>
								</xsl:if>
							</xsl:if>
						</level2>
						<level2>
							<h2>Børs</h2>
							<p>Børsnyheter fra hele verden.</p>
							<xsl:variable name="currentUniqueName30" select="document(concat($uniqueNamesPath, 'uniqueNames.xml'))/category/group[30]/uniqueName"/>
							<xsl:if test="$currentUniqueName30">
								<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName30]">
									<level3>
										<h3 class="e24">Oslo børs</h3>
										<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName30]">
											<level4>
												<xsl:apply-templates select="../../innhold"/>
											</level4>
										</xsl:for-each>
									</level3>
								</xsl:if>
							</xsl:if>
							<xsl:variable name="currentUniqueName31" select="document(concat($uniqueNamesPath, 'uniqueNames.xml'))/category/group[31]/uniqueName"/>
							<xsl:if test="$currentUniqueName31">
								<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName31]">
									<level3>
										<h3 class="e24">Europeiske børser</h3>
										<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName31]">
											<level4>
												<xsl:apply-templates select="../../innhold"/>
											</level4>
										</xsl:for-each>
									</level3>
								</xsl:if>
							</xsl:if>
							<xsl:variable name="currentUniqueName32" select="document(concat($uniqueNamesPath, 'uniqueNames.xml'))/category/group[32]/uniqueName"/>
							<xsl:if test="$currentUniqueName32">
								<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName32]">
									<level3>
										<h3 class="e24">Verdens børser</h3>
										<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName32]">
											<level4>
												<xsl:apply-templates select="../../innhold"/>
											</level4>
										</xsl:for-each>
									</level3>
								</xsl:if>
							</xsl:if>
							<xsl:variable name="currentUniqueName33" select="document(concat($uniqueNamesPath, 'uniqueNames.xml'))/category/group[33]/uniqueName"/>
							<xsl:if test="$currentUniqueName33">
								<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName33]">
									<level3>
										<h3 class="e24">Aksjere</h3>
										<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName33]">
											<level4>
												<xsl:apply-templates select="../../innhold"/>
											</level4>
										</xsl:for-each>
									</level3>
								</xsl:if>
							</xsl:if>
							<xsl:variable name="currentUniqueName34" select="document(concat($uniqueNamesPath, 'uniqueNames.xml'))/category/group[34]/uniqueName"/>
							<xsl:if test="$currentUniqueName34">
								<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName34]">
									<level3>
										<h3 class="e24">Fond</h3>
										<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName34]">
											<level4>
												<xsl:apply-templates select="../../innhold"/>
											</level4>
										</xsl:for-each>
									</level3>
								</xsl:if>
							</xsl:if>
							<xsl:variable name="currentUniqueName35" select="document(concat($uniqueNamesPath, 'uniqueNames.xml'))/category/group[35]/uniqueName"/>
							<xsl:if test="$currentUniqueName35">
								<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName35]">
									<level3>
										<h3 class="e24">Valuta</h3>
										<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName35]">
											<level4>
												<xsl:apply-templates select="../../innhold"/>
											</level4>
										</xsl:for-each>
									</level3>
								</xsl:if>
							</xsl:if>
							<xsl:variable name="currentUniqueName36" select="document(concat($uniqueNamesPath, 'uniqueNames.xml'))/category/group[36]/uniqueName"/>
							<xsl:if test="$currentUniqueName36">
								<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName36]">
									<level3>
										<h3 class="e24">Renter</h3>
										<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName36]">
											<level4>
												<xsl:apply-templates select="../../innhold"/>
											</level4>
										</xsl:for-each>
									</level3>
								</xsl:if>
							</xsl:if>
							<xsl:variable name="currentUniqueName37" select="document(concat($uniqueNamesPath, 'uniqueNames.xml'))/category/group[37]/uniqueName"/>
							<xsl:if test="$currentUniqueName37">
								<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName37]">
									<level3>
										<h3 class="e24">Olje og andre råvarer</h3>
										<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName37]">
											<level4>
												<xsl:apply-templates select="../../innhold"/>
											</level4>
										</xsl:for-each>
									</level3>
								</xsl:if>
							</xsl:if>
							<xsl:variable name="currentUniqueName38" select="document(concat($uniqueNamesPath, 'uniqueNames.xml'))/category/group[38]/uniqueName"/>
							<xsl:if test="$currentUniqueName38">
								<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName38]">
									<level3>
										<h3 class="e24">Indexer</h3>
										<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName38]">
											<level4>
												<xsl:apply-templates select="../../innhold"/>
											</level4>
										</xsl:for-each>
									</level3>
								</xsl:if>
							</xsl:if>
							<xsl:variable name="currentUniqueName39" select="document(concat($uniqueNamesPath, 'uniqueNames.xml'))/category/group[39]/uniqueName"/>
							<xsl:if test="$currentUniqueName39">
								<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName39]">
									<level3>
										<h3 class="e24">Derivater</h3>
										<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName39]">
											<level4>
												<xsl:apply-templates select="../../innhold"/>
											</level4>
										</xsl:for-each>
									</level3>
								</xsl:if>
							</xsl:if>
						</level2>

						<xsl:variable name="currentUniqueName40" select="document(concat($uniqueNamesPath, 'uniqueNames.xml'))/category/group[40]/uniqueName"/>
						<xsl:if test="$currentUniqueName40">
							<xsl:if test="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName40]">
								<level2>
									<h2>
										<xsl:value-of select="$id[40]"/>
									</h2>
									<p>Kommentarer fra E24.</p>
									<xsl:for-each select="artikkel/metainformasjon/seksjoner[uniqueName=$currentUniqueName40]">
										<level3>
											<xsl:apply-templates select="../../innhold"/>
										</level3>
									</xsl:for-each>
								</level2>
							</xsl:if>
						</xsl:if>
					</level1>
				</bodymatter>
			</book>
		</dtbook>
	</xsl:template>
</xsl:stylesheet><!-- Stylus Studio meta-information - (c) 2004-2007. Progress Software Corporation. All rights reserved.
<metaInformation>
<scenarios/><MapperMetaTag><MapperInfo srcSchemaPathIsRelative="yes" srcSchemaInterpretAsXML="no" destSchemaPath="" destSchemaRoot="" destSchemaPathIsRelative="yes" destSchemaInterpretAsXML="no"/><MapperBlockPosition></MapperBlockPosition><TemplateContext></TemplateContext><MapperFilter side="source"></MapperFilter></MapperMetaTag>
</metaInformation>
-->