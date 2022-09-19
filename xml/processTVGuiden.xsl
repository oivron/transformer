<?xml version="1.0" encoding="iso-8859-1"?>
<!DOCTYPE stylesheet [
<!ENTITY UPPERCASE "ABCDEFGHIJKLMNOPQRSTUVWXYZ">
<!ENTITY LOWERCASE "abcdefghijklmnopqrstuvwxyz">
<!ENTITY UPPER_TO_LOWER " '&UPPERCASE;' , '&LOWERCASE;' ">
<!ENTITY LOWER_TO_UPPER " '&LOWERCASE;' , '&UPPERCASE;' ">
]>
<!--<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/1999/xhtml" xmlns:xhtml="http://www.w3.org/1999/xhtml" exclude-result-prefixes="xhtml">

<xsl:output method="xhtml" version="1.0" doctype-public="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd" encoding="UTF-8" indent="yes"/>

	<!-- Pr. kanal, sortert på tid og med inndeling av dagen i formiddag, ettermiddag, kveld, natt. Utvalg 
	av kanaler (UPC grunnpakke). Pga. at det ikke er mulig å navigere på listeelementer i Daisy 2, har jeg 
	gått bort fra det. Bruker i stedet <h4> på programtittel og klokkeslett, mens programbeskrivelsen blir en <p>. 
	Der hvor et program består av flere programmer (eks. Distriktsnyheter), bruker jeg en <ul> i stedet for <p>. -->

	<!-- Skriver overskriftene i dokumentet og beregner dag, dato og måned for den første dagen i TVGuiden. -->
	<xsl:template match="Workbook">
		<xsl:param name="date-time" select="Worksheet/Table/Row/Cell[1]"/>
		<xsl:param name="date" select="substring-before($date-time,'T')"/>
		<xsl:param name="month" select="substring-before(substring-after($date,'-'),'-')"/>
		<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="no">
			<head>
				<title>TV guiden i Daisy format - inndelt etter kanal</title>
				<link href="tvguide.css" rel="stylesheet" type="text/css" media="all"/>
			</head>
			<body>
				<!-- Overskrift med dag, dato og måned. -->
				<h1>TV guiden i Daisy format for uken som starter 
					<xsl:variable name="day-of-the-week">
						<xsl:call-template name="calculate-day-of-the-week">
							<xsl:with-param name="date-time" select="$date-time"/>
						</xsl:call-template>
					</xsl:variable>
					<xsl:call-template name="get-day-of-the-week-name-case">
						<xsl:with-param name="day-of-the-week" select="$day-of-the-week"/>
					</xsl:call-template>
					<xsl:text> </xsl:text>
					<xsl:value-of select="substring(Worksheet/Table/Row/Cell, 9, 2)"/>.
					<xsl:call-template name="get-month-name">
						<xsl:with-param name="month" select="$month"/>
					</xsl:call-template>
					 &#x2013; inndelt etter kanal
				</h1>
				<h1>Om TV guiden</h1>
				<p>TV guiden er tilrettelagt av Norsk lyd og blindeskriftsbibliotek. TV guiden er i Daisy 2 0 2 
				fulltekstformat med syntetisk tale. Lydboken må ikke videredistribueres. CD med TV guide samt 
				medfølgende konvolutt skal ikke returneres til NLB. CD kan kastes sammen med øvrig husholdningsavfall.</p>
				<p>Vi er takknemlig for kommentarer og tilbakemeldinger, fortrinnsvis på e-post: lydavis@nlb.no.</p>
				<h1>Hvordan benytte TV guiden</h1>
				<p>TV-guiden gir deg programbildet for 17 TV-kanaler. Du kan forflytte deg på 4 navigasjonsnivåer med din 
				Daisy spiller.</p>
				<p>På nivå 1 navigerer du deg på ukedag. TV guiden inneholder programmet for perioden tirsdag til og med 
				neste tirsdag. På nivå 2 kan du navigere på TV kanal. På nivå 3 kan du navigere på en tidsmessig del av 
				dagen. Dagen er delt inn i formiddag, ettermiddag, kveld og natt. På nivå 4 finner du programbeskrivelse. 
				Her får du informasjon om oppstartstidspunkt, programtittel, TV-kanal samt en beskrivelse av programmet. 
				Episodenummer blir angitt i teksten og fremstår som et nummer i parentes.</p>
				<xsl:apply-templates select="/Workbook" mode="time"/>
			</body>
		</html>
	</xsl:template>

	<!-- I feeden ligger alle dagene i hver sin Worksheet. Denne lager overskrift for hver av dagene i TVGuiden. -->
	<xsl:template match="Workbook" mode="time">
		<xsl:for-each select="Worksheet">
			<h1>
				<xsl:apply-templates select="Table" mode="time"/>
			</h1>
			<xsl:apply-templates select="Table"/>
		</xsl:for-each>
	</xsl:template>

	<!-- Beregner dag, dato og måned for hver av dagene i TVGuiden. -->
	<xsl:template match="Table" mode="time">
		<xsl:param name="date-time" select="Row/Cell[1]"/>
		<xsl:param name="date" select="substring-before($date-time,'T')"/>
		<xsl:param name="month" select="substring-before(substring-after($date,'-'),'-')"/>

		<xsl:variable name="day-of-the-week">
			<xsl:call-template name="calculate-day-of-the-week">
				<xsl:with-param name="date-time" select="$date-time"/>
			</xsl:call-template>
		</xsl:variable>
		<xsl:call-template name="get-day-of-the-week-name">
			<xsl:with-param name="day-of-the-week" select="$day-of-the-week"/>
		</xsl:call-template>
		<xsl:text> </xsl:text>
		<xsl:value-of select="substring(Row/Cell, 9, 2)"/>.
		<xsl:call-template name="get-month-name">
			<xsl:with-param name="month" select="$month"/>
		</xsl:call-template>
	</xsl:template>

	<!-- Beregner navn på dag som står etter kanalnavnene -->
	<xsl:template match="Table" mode="day">
		<xsl:param name="date-time" select="Row/Cell[1]"/>
		<xsl:param name="date" select="substring-before($date-time,'T')"/>

		<xsl:variable name="day-of-the-week">
			<xsl:call-template name="calculate-day-of-the-week">
				<xsl:with-param name="date-time" select="$date-time"/>
			</xsl:call-template>
		</xsl:variable>
		<xsl:call-template name="get-day-of-the-week-name-case">
			<xsl:with-param name="day-of-the-week" select="$day-of-the-week"/>
		</xsl:call-template>
	</xsl:template>

	<!-- For hver kanal gjør jeg 4 utvalg der jeg sjekker på kanal og et tidsrom på dagen. -->
	<xsl:template match="Worksheet/Table">
		<xsl:if test="Row/Cell[2]='nrktv1'">
			<h2>NRK1: <xsl:apply-templates select="." mode="day"/></h2>
			<h3>Formiddag</h3>
			<xsl:apply-templates select="Row[Cell[2]='nrktv1'][Cell[3]&lt;12][Cell[3]&gt;=05]" mode="prog"/>
			<h3>Ettermiddag</h3>
			<xsl:apply-templates select="Row[Cell[2]='nrktv1'][Cell[3]&lt;18][Cell[3]&gt;=12]" mode="prog"/>
			<h3>Kveld</h3>
			<xsl:apply-templates select="Row[Cell[2]='nrktv1'][Cell[3]&gt;=18]" mode="prog"/>
			<h3>Natt</h3>
			<xsl:apply-templates select="Row[Cell[2]='nrktv1'][Cell[3]&lt;05]" mode="prog"/>
		</xsl:if>
		<xsl:if test="Row/Cell[2]='nrktv2'">
			<h2>NRK2: <xsl:apply-templates select="." mode="day"/></h2>
			<h3>Formiddag</h3>
			<xsl:apply-templates select="Row[Cell[2]='nrktv2'][Cell[3]&lt;12][Cell[3]&gt;=05]" mode="prog"/>
			<h3>Ettermiddag</h3>
			<xsl:apply-templates select="Row[Cell[2]='nrktv2'][Cell[3]&lt;18][Cell[3]&gt;=12]" mode="prog"/>
			<h3>Kveld</h3>
			<xsl:apply-templates select="Row[Cell[2]='nrktv2'][Cell[3]&gt;=18]" mode="prog"/>
			<h3>Natt</h3>
			<xsl:apply-templates select="Row[Cell[2]='nrktv2'][Cell[3]&lt;05]" mode="prog"/>
		</xsl:if>
		<xsl:if test="Row/Cell[2]='nrk3'">
			<h2>NRK3: <xsl:apply-templates select="." mode="day"/></h2>
			<h3>Formiddag</h3>
			<xsl:apply-templates select="Row[Cell[2]='nrk3'][Cell[3]&lt;12][Cell[3]&gt;=05]" mode="prog"/>
			<h3>Ettermiddag</h3>
			<xsl:apply-templates select="Row[Cell[2]='nrk3'][Cell[3]&lt;18][Cell[3]&gt;=12]" mode="prog"/>
			<h3>Kveld</h3>
			<xsl:apply-templates select="Row[Cell[2]='nrk3'][Cell[3]&gt;=18]" mode="prog"/>
			<h3>Natt</h3>
			<xsl:apply-templates select="Row[Cell[2]='nrk3'][Cell[3]&lt;05]" mode="prog"/>
		</xsl:if>
		<xsl:if test="Row/Cell[2]='nrksuper'">
			<h2>NRK Super: <xsl:apply-templates select="." mode="day"/></h2>
			<h3>Formiddag</h3>
			<xsl:apply-templates select="Row[Cell[2]='nrksuper'][Cell[3]&lt;12][Cell[3]&gt;=05]" mode="prog"/>
			<h3>Ettermiddag</h3>
			<xsl:apply-templates select="Row[Cell[2]='nrksuper'][Cell[3]&lt;18][Cell[3]&gt;=12]" mode="prog"/>
			<h3>Kveld</h3>
			<xsl:apply-templates select="Row[Cell[2]='nrksuper'][Cell[3]&gt;=18]" mode="prog"/>
			<h3>Natt</h3>
			<xsl:apply-templates select="Row[Cell[2]='nrksuper'][Cell[3]&lt;05]" mode="prog"/>
		</xsl:if>
		<xsl:if test="Row/Cell[2]='tv2'">
			<h2>TV2: <xsl:apply-templates select="." mode="day"/></h2>
			<h3>Formiddag</h3>
			<xsl:apply-templates select="Row[Cell[2]='tv2'][Cell[3]&lt;12][Cell[3]&gt;=05]" mode="prog"/>
			<h3>Ettermiddag</h3>
			<xsl:apply-templates select="Row[Cell[2]='tv2'][Cell[3]&lt;18][Cell[3]&gt;=12]" mode="prog"/>
			<h3>Kveld</h3>
			<xsl:apply-templates select="Row[Cell[2]='tv2'][Cell[3]&gt;=18]" mode="prog"/>
			<h3>Natt</h3>
			<xsl:apply-templates select="Row[Cell[2]='tv2'][Cell[3]&lt;05]" mode="prog"/>
		</xsl:if>
		<xsl:if test="Row/Cell[2]='tvn'">
			<h2>TV NORGE: <xsl:apply-templates select="." mode="day"/></h2>
			<h3>Formiddag</h3>
			<xsl:apply-templates select="Row[Cell[2]='tvn'][Cell[3]&lt;12][Cell[3]&gt;=05]" mode="prog"/>
			<h3>Ettermiddag</h3>
			<xsl:apply-templates select="Row[Cell[2]='tvn'][Cell[3]&lt;18][Cell[3]&gt;=12]" mode="prog"/>
			<h3>Kveld</h3>
			<xsl:apply-templates select="Row[Cell[2]='tvn'][Cell[3]&gt;=18]" mode="prog"/>
			<h3>Natt</h3>
			<xsl:apply-templates select="Row[Cell[2]='tvn'][Cell[3]&lt;05]" mode="prog"/>
		</xsl:if>
		<xsl:if test="Row/Cell[2]='tv2zebra'">
			<h2>TV2 Zebra: <xsl:apply-templates select="." mode="day"/></h2>
			<h3>Formiddag</h3>
			<xsl:apply-templates select="Row[Cell[2]='tv2zebra'][Cell[3]&lt;12][Cell[3]&gt;=05]" mode="prog"/>
			<h3>Ettermiddag</h3>
			<xsl:apply-templates select="Row[Cell[2]='tv2zebra'][Cell[3]&lt;18][Cell[3]&gt;=12]" mode="prog"/>
			<h3>Kveld</h3>
			<xsl:apply-templates select="Row[Cell[2]='tv2zebra'][Cell[3]&gt;=18]" mode="prog"/>
			<h3>Natt</h3>
			<xsl:apply-templates select="Row[Cell[2]='tv2zebra'][Cell[3]&lt;05]" mode="prog"/>
		</xsl:if>
		<xsl:if test="Row/Cell[2]='tv2nyhet'">
			<h2>TV2 Nyhetskanalen: <xsl:apply-templates select="." mode="day"/></h2>
			<h3>Formiddag</h3>
			<xsl:apply-templates select="Row[Cell[2]='tv2nyhet'][Cell[3]&lt;12][Cell[3]&gt;=05]" mode="prog"/>
			<h3>Ettermiddag</h3>
			<xsl:apply-templates select="Row[Cell[2]='tv2nyhet'][Cell[3]&lt;18][Cell[3]&gt;=12]" mode="prog"/>
			<h3>Kveld</h3>
			<xsl:apply-templates select="Row[Cell[2]='tv2nyhet'][Cell[3]&gt;=18]" mode="prog"/>
			<h3>Natt</h3>
			<xsl:apply-templates select="Row[Cell[2]='tv2nyhet'][Cell[3]&lt;05]" mode="prog"/>
		</xsl:if>
		<xsl:if test="Row/Cell[2]='tv2film'">
			<h2>TV2 Filmkanalen: <xsl:apply-templates select="." mode="day"/></h2>
			<h3>Formiddag</h3>
			<xsl:apply-templates select="Row[Cell[2]='tv2film'][Cell[3]&lt;12][Cell[3]&gt;=05]" mode="prog"/>
			<h3>Ettermiddag</h3>
			<xsl:apply-templates select="Row[Cell[2]='tv2film'][Cell[3]&lt;18][Cell[3]&gt;=12]" mode="prog"/>
			<h3>Kveld</h3>
			<xsl:apply-templates select="Row[Cell[2]='tv2film'][Cell[3]&gt;=18]" mode="prog"/>
			<h3>Natt</h3>
			<xsl:apply-templates select="Row[Cell[2]='tv2film'][Cell[3]&lt;05]" mode="prog"/>
		</xsl:if>
		<xsl:if test="Row/Cell[2]='tv3'">
			<h2>TV3: <xsl:apply-templates select="." mode="day"/></h2>
			<h3>Formiddag</h3>
			<xsl:apply-templates select="Row[Cell[2]='tv3'][Cell[3]&lt;12][Cell[3]&gt;=05]" mode="prog"/>
			<h3>Ettermiddag</h3>
			<xsl:apply-templates select="Row[Cell[2]='tv3'][Cell[3]&lt;18][Cell[3]&gt;=12]" mode="prog"/>
			<h3>Kveld</h3>
			<xsl:apply-templates select="Row[Cell[2]='tv3'][Cell[3]&gt;=18]" mode="prog"/>
			<h3>Natt</h3>
			<xsl:apply-templates select="Row[Cell[2]='tv3'][Cell[3]&lt;05]" mode="prog"/>
		</xsl:if>
		<xsl:if test="Row/Cell[2]='fem'">
			<h2>FEM: <xsl:apply-templates select="." mode="day"/></h2>
			<h3>Formiddag</h3>
			<xsl:apply-templates select="Row[Cell[2]='fem'][Cell[3]&lt;12][Cell[3]&gt;=05]" mode="prog"/>
			<h3>Ettermiddag</h3>
			<xsl:apply-templates select="Row[Cell[2]='fem'][Cell[3]&lt;18][Cell[3]&gt;=12]" mode="prog"/>
			<h3>Kveld</h3>
			<xsl:apply-templates select="Row[Cell[2]='fem'][Cell[3]&gt;=18]" mode="prog"/>
			<h3>Natt</h3>
			<xsl:apply-templates select="Row[Cell[2]='fem'][Cell[3]&lt;05]" mode="prog"/>
		</xsl:if>
		<xsl:if test="Row/Cell[2]='ztvsve'">
			<h2>ZTV: <xsl:apply-templates select="." mode="day"/></h2>
			<h3>Formiddag</h3>
			<xsl:apply-templates select="Row[Cell[2]='ztvsve'][Cell[3]&lt;12][Cell[3]&gt;=05]" mode="prog"/>
			<h3>Ettermiddag</h3>
			<xsl:apply-templates select="Row[Cell[2]='ztvsve'][Cell[3]&lt;18][Cell[3]&gt;=12]" mode="prog"/>
			<h3>Kveld</h3>
			<xsl:apply-templates select="Row[Cell[2]='ztvsve'][Cell[3]&gt;=18]" mode="prog"/>
			<h3>Natt</h3>
			<xsl:apply-templates select="Row[Cell[2]='ztvsve'][Cell[3]&lt;05]" mode="prog"/>
		</xsl:if>
		<xsl:if test="Row/Cell[2]='viasat4'">
			<h2>Viasat4: <xsl:apply-templates select="." mode="day"/></h2>
			<h3>Formiddag</h3>
			<xsl:apply-templates select="Row[Cell[2]='viasat4'][Cell[3]&lt;12][Cell[3]&gt;=05]" mode="prog"/>
			<h3>Ettermiddag</h3>
			<xsl:apply-templates select="Row[Cell[2]='viasat4'][Cell[3]&lt;18][Cell[3]&gt;=12]" mode="prog"/>
			<h3>Kveld</h3>
			<xsl:apply-templates select="Row[Cell[2]='viasat4'][Cell[3]&gt;=18]" mode="prog"/>
			<h3>Natt</h3>
			<xsl:apply-templates select="Row[Cell[2]='viasat4'][Cell[3]&lt;05]" mode="prog"/>
		</xsl:if>
		<xsl:if test="Row/Cell[2]='svtv1'">
			<h2>SVT1: <xsl:apply-templates select="." mode="day"/></h2>
			<h3>Formiddag</h3>
			<xsl:apply-templates select="Row[Cell[2]='svtv1'][Cell[3]&lt;12][Cell[3]&gt;=05]" mode="prog"/>
			<h3>Ettermiddag</h3>
			<xsl:apply-templates select="Row[Cell[2]='svtv1'][Cell[3]&lt;18][Cell[3]&gt;=12]" mode="prog"/>
			<h3>Kveld</h3>
			<xsl:apply-templates select="Row[Cell[2]='svtv1'][Cell[3]&gt;=18]" mode="prog"/>
			<h3>Natt</h3>
			<xsl:apply-templates select="Row[Cell[2]='svtv1'][Cell[3]&lt;05]" mode="prog"/>
		</xsl:if>
		<xsl:if test="Row/Cell[2]='svtv2'">
			<h2>SVT2: <xsl:apply-templates select="." mode="day"/></h2>
			<h3>Formiddag</h3>
			<xsl:apply-templates select="Row[Cell[2]='svtv2'][Cell[3]&lt;12][Cell[3]&gt;=05]" mode="prog"/>
			<h3>Ettermiddag</h3>
			<xsl:apply-templates select="Row[Cell[2]='svtv2'][Cell[3]&lt;18][Cell[3]&gt;=12]" mode="prog"/>
			<h3>Kveld</h3>
			<xsl:apply-templates select="Row[Cell[2]='svtv2'][Cell[3]&gt;=18]" mode="prog"/>
			<h3>Natt</h3>
			<xsl:apply-templates select="Row[Cell[2]='svtv2'][Cell[3]&lt;05]" mode="prog"/>
		</xsl:if>
		<xsl:if test="Row/Cell[2]='tv4'">
			<h2>TV4: <xsl:apply-templates select="." mode="day"/></h2>
			<h3>Formiddag</h3>
			<xsl:apply-templates select="Row[Cell[2]='tv4'][Cell[3]&lt;12][Cell[3]&gt;=05]" mode="prog"/>
			<h3>Ettermiddag</h3>
			<xsl:apply-templates select="Row[Cell[2]='tv4'][Cell[3]&lt;18][Cell[3]&gt;=12]" mode="prog"/>
			<h3>Kveld</h3>
			<xsl:apply-templates select="Row[Cell[2]='tv4'][Cell[3]&gt;=18]" mode="prog"/>
			<h3>Natt</h3>
			<xsl:apply-templates select="Row[Cell[2]='tv4'][Cell[3]&lt;05]" mode="prog"/>
		</xsl:if>
		<xsl:if test="Row/Cell[2]='animal'">
			<h2>Animal Planet: <xsl:apply-templates select="." mode="day"/></h2>
			<h3>Formiddag</h3>
			<xsl:apply-templates select="Row[Cell[2]='animal'][Cell[3]&lt;12][Cell[3]&gt;=05]" mode="prog"/>
			<h3>Ettermiddag</h3>
			<xsl:apply-templates select="Row[Cell[2]='animal'][Cell[3]&lt;18][Cell[3]&gt;=12]" mode="prog"/>
			<h3>Kveld</h3>
			<xsl:apply-templates select="Row[Cell[2]='animal'][Cell[3]&gt;=18]" mode="prog"/>
			<h3>Natt</h3>
			<xsl:apply-templates select="Row[Cell[2]='animal'][Cell[3]&lt;05]" mode="prog"/>
		</xsl:if>
		<xsl:if test="Row/Cell[2]='cnn'">
			<h2>CNN: <xsl:apply-templates select="." mode="day"/></h2>
			<h3>Formiddag</h3>
			<xsl:apply-templates select="Row[Cell[2]='cnn'][Cell[3]&lt;12][Cell[3]&gt;=05]" mode="prog"/>
			<h3>Ettermiddag</h3>
			<xsl:apply-templates select="Row[Cell[2]='cnn'][Cell[3]&lt;18][Cell[3]&gt;=12]" mode="prog"/>
			<h3>Kveld</h3>
			<xsl:apply-templates select="Row[Cell[2]='cnn'][Cell[3]&gt;=18]" mode="prog"/>
			<h3>Natt</h3>
			<xsl:apply-templates select="Row[Cell[2]='cnn'][Cell[3]&lt;05]" mode="prog"/>
		</xsl:if>
		<xsl:if test="Row/Cell[2]='bbcprime'">
			<h2>BBC Prime: <xsl:apply-templates select="." mode="day"/></h2>
			<h3>Formiddag</h3>
			<xsl:apply-templates select="Row[Cell[2]='bbcprime'][Cell[3]&lt;12][Cell[3]&gt;=05]" mode="prog"/>
			<h3>Ettermiddag</h3>
			<xsl:apply-templates select="Row[Cell[2]='bbcprime'][Cell[3]&lt;18][Cell[3]&gt;=12]" mode="prog"/>
			<h3>Kveld</h3>
			<xsl:apply-templates select="Row[Cell[2]='bbcprime'][Cell[3]&gt;=18]" mode="prog"/>
			<h3>Natt</h3>
			<xsl:apply-templates select="Row[Cell[2]='bbcprime'][Cell[3]&lt;05]" mode="prog"/>
		</xsl:if>
		<xsl:if test="Row/Cell[2]='discov'">
			<h2>Discovery: <xsl:apply-templates select="." mode="day"/></h2>
			<h3>Formiddag</h3>
			<xsl:apply-templates select="Row[Cell[2]='discov'][Cell[3]&lt;12][Cell[3]&gt;=05]" mode="prog"/>
			<h3>Ettermiddag</h3>
			<xsl:apply-templates select="Row[Cell[2]='discov'][Cell[3]&lt;18][Cell[3]&gt;=12]" mode="prog"/>
			<h3>Kveld</h3>
			<xsl:apply-templates select="Row[Cell[2]='discov'][Cell[3]&gt;=18]" mode="prog"/>
			<h3>Natt</h3>
			<xsl:apply-templates select="Row[Cell[2]='discov'][Cell[3]&lt;05]" mode="prog"/>
		</xsl:if>
		<xsl:if test="Row/Cell[2]='natgeoch'">
			<h2>National Geographic: <xsl:apply-templates select="." mode="day"/></h2>
			<h3>Formiddag</h3>
			<xsl:apply-templates select="Row[Cell[2]='natgeoch'][Cell[3]&lt;12][Cell[3]&gt;=05]" mode="prog"/>
			<h3>Ettermiddag</h3>
			<xsl:apply-templates select="Row[Cell[2]='natgeoch'][Cell[3]&lt;18][Cell[3]&gt;=12]" mode="prog"/>
			<h3>Kveld</h3>
			<xsl:apply-templates select="Row[Cell[2]='natgeoch'][Cell[3]&gt;=18]" mode="prog"/>
			<h3>Natt</h3>
			<xsl:apply-templates select="Row[Cell[2]='natgeoch'][Cell[3]&lt;05]" mode="prog"/>
		</xsl:if>
		<xsl:if test="Row/Cell[2]='eurospo'">
			<h2>Eurosport: <xsl:apply-templates select="." mode="day"/></h2>
			<h3>Formiddag</h3>
			<xsl:apply-templates select="Row[Cell[2]='eurospo'][Cell[3]&lt;12][Cell[3]&gt;=05]" mode="prog"/>
			<h3>Ettermiddag</h3>
			<xsl:apply-templates select="Row[Cell[2]='eurospo'][Cell[3]&lt;18][Cell[3]&gt;=12]" mode="prog"/>
			<h3>Kveld</h3>
			<xsl:apply-templates select="Row[Cell[2]='eurospo'][Cell[3]&gt;=18]" mode="prog"/>
			<h3>Natt</h3>
			<xsl:apply-templates select="Row[Cell[2]='eurospo'][Cell[3]&lt;05]" mode="prog"/>
		</xsl:if>
	</xsl:template>

	<!-- Sjekker om et program består av flere underprogram eller ikke. Behandler programmene avhengig av dette. -->
	<xsl:template match="Row" mode="prog">
		<!-- Sjekker om den syvende cellen i en rad inneholder noe. Det betyr at det er et program som består 
		av flere programmer (eks. Distriktsnyheter). -->
		<xsl:if test="Cell[7]!=' '">
			<!-- Programmer som inngår i et fellesprogram (eks. Distriktsnyheter) legges i en liste. -->
			<h4>
				<xsl:apply-templates select="." mode="progtittel"/>
			</h4>
			<p>
				<xsl:apply-templates select="." mode="progkategori"/>
			</p>
			<ul>
				<!-- Oppretter en variabel "tidspunkt" som inneholder verdien av Celle 7 (dvs. kanal/ååååmmdd/klokkeslett).
				Nedenfor bruker jeg denne for å sammenligne med verdien av Celle 8 i etterfølgende 
				noder (following-siblings). -->
				<xsl:variable name="tidspunkt">
					<xsl:value-of select="Cell[7]"/>
				</xsl:variable>
				<!-- Jeg gjør en sjekk på etterfølgende noder for å finne ut hvorvidt de er et underprogram 
				av et annet program. For at det skal være tilfelle, må Celle 7 være tom samtidig som verdien 
				av Celle 8 må være lik variabelen "tidspunkt". -->
				<xsl:for-each select="following-sibling::*[Cell[8]=$tidspunkt][Cell[7]=' ']">
					<li>
						<xsl:apply-templates select="." mode="progtittel"/>
					</li>
				</xsl:for-each>
			</ul>
		</xsl:if>
		<!-- Normaltilfelle for program som ikke er del av annet program. -->
		<xsl:if test="self::*[Cell[7]=' '][Cell[8]=' ']">
			<h4>
				<xsl:apply-templates select="." mode="progtittel"/>
			</h4>
			<p>
				<xsl:apply-templates select="." mode="progkategori"/>
			</p>
			<p>
				<xsl:apply-templates select="." mode="progbeskrivelse"/>
			</p>
		</xsl:if>
	</xsl:template>

	<!-- Klokkeslett og programtittel for et program. -->
	<xsl:template match="Row" mode="progtittel">
		Kl <xsl:value-of select="Cell[3]"/>, <xsl:value-of select="Cell[9]"/>
		<xsl:apply-templates select="." mode="reprise"/>
	</xsl:template>

	<!-- Celle 6 inneholder "REPRISE" hvis programmet er sendt tidligere. -->
	<xsl:template match="Row" mode="reprise">
		<xsl:if test="Cell[6]!=' '">
			<xsl:text> (reprise)</xsl:text>
		</xsl:if>
	</xsl:template>

	<!-- Programkategori for et program. Gjør om kategorinavnene slik at det blir stor forbokstav og resten små. -->
	<xsl:template match="Row" mode="progkategori">
		<!-- Behandler hovedkategori som ligger i Celle 5. -->
		<xsl:variable name="catFirst">
			<xsl:value-of select="substring(Cell[5], 1, 1)"/>
		</xsl:variable>
		<xsl:variable name="catRestCaps">
			<xsl:value-of select="substring(Cell[5], 2, string-length())"/>
		</xsl:variable>
		<xsl:variable name="catRest">
			<xsl:value-of select="translate($catRestCaps, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ' , 'abcdefghijklmnopqrstuvwxyz' )"/>
		</xsl:variable>
		<xsl:value-of select="concat($catFirst, $catRest)"/>
		<xsl:text>.</xsl:text>
	</xsl:template>

	<!-- Programbeskrivelse for et program. -->
	<xsl:template match="Row" mode="progbeskrivelse">
		<xsl:value-of select="Cell[11]"/>
	</xsl:template>

	<!-- Beregner dagnummer. -->
	<xsl:template name="calculate-day-of-the-week">
		<xsl:param name="date-time"/>
		<xsl:param name="date" select="substring-before($date-time,'T')"/>
		<xsl:param name="year" select="substring-before($date,'-')"/>
		<xsl:param name="month" select="substring-before(substring-after($date,'-'),'-')"/>
		<xsl:param name="day" select="substring-after(substring-after($date,'-'),'-')"/>

		<xsl:variable name="a" select="floor((14 - $month) div 12)"/>
		<xsl:variable name="y" select="$year - $a"/>
		<xsl:variable name="m" select="$month + 12 * $a - 2"/>

		<xsl:value-of select="($day + $y + floor($y div 4) - floor($y div 100) + floor($y div 400) + floor((31 * $m) div 12)) mod 7"/>
	</xsl:template>

	<!-- Beregner navn på dag på grunnlag av dagnummer (stor forbokstav). -->
	<xsl:template name="get-day-of-the-week-name">
		<xsl:param name="day-of-the-week"/>
		<xsl:choose>
			<xsl:when test="$day-of-the-week = 0">Søndag</xsl:when>
			<xsl:when test="$day-of-the-week = 1">Mandag</xsl:when>
			<xsl:when test="$day-of-the-week = 2">Tirsdag</xsl:when>
			<xsl:when test="$day-of-the-week = 3">Onsdag</xsl:when>
			<xsl:when test="$day-of-the-week = 4">Torsdag</xsl:when>
			<xsl:when test="$day-of-the-week = 5">Fredag</xsl:when>
			<xsl:when test="$day-of-the-week = 6">Lørdag</xsl:when>
			<xsl:otherwise>error: <xsl:value-of select="$day-of-the-week"/></xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- Beregner navn på dag på grunnlag av dagnummer (liten forbokstav). -->
	<xsl:template name="get-day-of-the-week-name-case">
		<xsl:param name="day-of-the-week"/>
		<xsl:choose>
			<xsl:when test="$day-of-the-week = 0">søndag</xsl:when>
			<xsl:when test="$day-of-the-week = 1">mandag</xsl:when>
			<xsl:when test="$day-of-the-week = 2">tirsdag</xsl:when>
			<xsl:when test="$day-of-the-week = 3">onsdag</xsl:when>
			<xsl:when test="$day-of-the-week = 4">torsdag</xsl:when>
			<xsl:when test="$day-of-the-week = 5">fredag</xsl:when>
			<xsl:when test="$day-of-the-week = 6">lørdag</xsl:when>
			<xsl:otherwise>error: <xsl:value-of select="$day-of-the-week"/></xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- Beregner navn på måned. -->
	<xsl:template name="get-month-name">
		<xsl:param name="date-time"/>
		<xsl:param name="date" select="substring-before($date-time,'T')"/>
		<xsl:param name="month" select="substring-before(substring-after($date,'-'),'-')"/>

		<xsl:choose>
			<xsl:when test="$month = 1">januar</xsl:when>
			<xsl:when test="$month = 2">februar</xsl:when>
			<xsl:when test="$month = 3">mars</xsl:when>
			<xsl:when test="$month = 4">april</xsl:when>
			<xsl:when test="$month = 5">mai</xsl:when>
			<xsl:when test="$month = 6">juni</xsl:when>
			<xsl:when test="$month = 7">juli</xsl:when>
			<xsl:when test="$month = 8">august</xsl:when>
			<xsl:when test="$month = 9">september</xsl:when>
			<xsl:when test="$month = 10">oktober</xsl:when>
			<xsl:when test="$month = 11">november</xsl:when>
			<xsl:when test="$month = 12">desember</xsl:when>
			<xsl:otherwise>error: <xsl:value-of select="$month"/></xsl:otherwise>
		</xsl:choose>
	</xsl:template>
</xsl:stylesheet><!-- Stylus Studio meta-information - (c) 2004-2007. Progress Software Corporation. All rights reserved.
<metaInformation>
<scenarios/><MapperMetaTag><MapperInfo srcSchemaPathIsRelative="yes" srcSchemaInterpretAsXML="no" destSchemaPath="" destSchemaRoot="" destSchemaPathIsRelative="yes" destSchemaInterpretAsXML="no"/><MapperBlockPosition></MapperBlockPosition><TemplateContext></TemplateContext><MapperFilter side="source"></MapperFilter></MapperMetaTag>
</metaInformation>
-->