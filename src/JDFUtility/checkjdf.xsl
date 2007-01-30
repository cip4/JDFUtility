<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="html"/>


<!-- here are the processing templates -->
<xsl:template match="CheckOutput">
<LINK REL="stylesheet" HREF="http://www.cip4.org/css/styles_pc.css" TYPE="text/css"/>
<head><title>CIP4 CheckJDF Output</title></head>
<body bgcolor="#ccccff">
<xsl:comment>#include virtual="/global/navigation/menue_switch.php?section=support" </xsl:comment> 
<HTML><H1>CheckJDF Validation Output</H1></HTML>
<br/>
<hr/>
<br/>
<xsl:text>Checker Version: </xsl:text><xsl:value-of select="@Version"/>
<br/>
<xsl:text>Raw XML Checker Output for: </xsl:text>
<xsl:element name="A">
<xsl:attribute name="href"><xsl:value-of select="@XMLUrl"/></xsl:attribute>
<xsl:value-of select="@XMLFile"/>
</xsl:element>


<xsl:apply-templates/>
<xsl:comment>#include virtual="/global/bottom_short.php" </xsl:comment> 
</body>
</xsl:template>

<!-- =============================================== -->

<xsl:template match="TestFile">
<hr/>
<H2> Testing File:
<xsl:value-of select="@FileName"/>
</H2>
		<br/>
<xsl:apply-templates/>
</xsl:template>

<!-- =============================================== -->

<xsl:template match="SchemaValidationOutput[@ValidationResult='Valid']">
<hr/>
<H3><font color="#00ff00">XML Schema Validation Successful:</font></H3>
</xsl:template>
<!-- =============================================== -->

<xsl:template match="SchemaValidationOutput">
<hr/>
<H3>XML Schema Validation Output: </H3>
<xsl:apply-templates/>
</xsl:template>

<!-- =============================================== -->
<xsl:template match="SchemaValidationOutput/Error">
<font color="#ff3333">Schema Error: </font><xsl:value-of select="@Message"/>
<br/>
<xsl:apply-templates/>
</xsl:template>

<!-- =============================================== -->
<xsl:template match="SchemaValidationOutput/FatalError">
<font color="#ff3333">Schema Fatal Error: </font><xsl:value-of select="@Message"/>
<br/>
<xsl:apply-templates/>
</xsl:template>

<!-- =============================================== -->
<xsl:template match="SchemaValidationOutput/Warning">
<font color="#aaaa00">Schema Warning: </font><xsl:value-of select="@Message"/>
<br/>
<xsl:apply-templates/>
</xsl:template>

<!-- =============================================== -->
<!-- =============================================== -->
<!-- =============================================== -->
<!-- =============================================== -->

<xsl:template match="CheckJDFOutput[@IsValid='true']">
<hr/>
<H3><font color="#00ff00">CheckJDF Validation Successful:</font></H3>
</xsl:template>

<!-- =============================================== -->

<xsl:template match="CheckJDFOutput">
<hr/>
<H3>CheckJDF Validation Output: </H3>
<xsl:apply-templates/>
</xsl:template>

<!-- =============================================== -->
<!-- =============================================== -->

<xsl:template match="TestElement[@ErrorType='InvalidElement']">
<!-- ===============================================
<H4>Invalid Element: <xsl:value-of select="@NodeName"/> 
at: <xsl:value-of select="@XPath"/> </H4>
<xsl:text>Message: </xsl:text> <xsl:value-of select="@Message"/>
 =============================================== -->
<xsl:apply-templates/>
</xsl:template>

<!-- =============================================== -->

<xsl:template match="TestElement[@ErrorType='PrivateElement']">
<H4>Private Element: <xsl:value-of select="@NodeName"/> 
<xsl:text> at: </xsl:text> <xsl:value-of select="@XPath"/> </H4>
<xsl:apply-templates/>
</xsl:template>

<!-- =============================================== -->

<xsl:template match="TestElement[@ErrorType='MissingElement']">
<H4>Missing Required Element: <xsl:value-of select="@NodeName"/> 
at: <xsl:value-of select="@XPath"/> </H4>
<xsl:apply-templates/>
</xsl:template>

<!-- =============================================== -->

<xsl:template match="TestElement[@ErrorType='UnknownElement']">
<H4>Unknown Element: <xsl:value-of select="@NodeName"/> 
at: <xsl:value-of select="@XPath"/> </H4>
<xsl:apply-templates/>
</xsl:template>

<!-- =============================================== -->

<xsl:template match="TestElement[@ErrorType='DeprecatedElement']">
<H4>Warning: Deprecated Element: <xsl:value-of select="@NodeName"/> 
at: <xsl:value-of select="@XPath"/> </H4>
<xsl:apply-templates/>
</xsl:template>

<!-- =============================================== -->

<xsl:template match="TestElement[@ErrorType='PrereleaseElement']">
<H4>Prerelease Element: <xsl:value-of select="@NodeName"/> 
at: <xsl:value-of select="@XPath"/> </H4>
<xsl:apply-templates/>
</xsl:template>

<!-- =============================================== -->

<!-- =============================================== -->

<xsl:template match="TestElement[@ErrorType='SwapElement']">
<H4>Attribute written as Element: <xsl:value-of select="@NodeName"/> 
at: <xsl:value-of select="@XPath"/> </H4>
<xsl:apply-templates/>
</xsl:template>

<!-- =============================================== -->

<xsl:template match="TestElement[@ErrorType='MissingResourceLink']">
<H4>Missing ResourceLink: <xsl:value-of select="@NodeName"/> 
<xsl:text> at: </xsl:text> <xsl:value-of select="@XPath"/> </H4>
<xsl:text>Message: </xsl:text> <xsl:value-of select="@Message"/>
<xsl:apply-templates/>
</xsl:template>

<!-- =============================================== -->

<xsl:template match="TestElement[@ErrorType='UnknownResourceLink']">
<H4>Missing ResourceLink: <xsl:value-of select="@NodeName"/> 
<xsl:text> at: </xsl:text> <xsl:value-of select="@XPath"/> </H4>
<xsl:text>Message: </xsl:text> <xsl:value-of select="@Message"/>
<xsl:apply-templates/>
</xsl:template>

<!-- =============================================== -->

<xsl:template match="TestElement[@ErrorType='MissingProcessUsage']">
<H4>Missing ProcessUsage for ResourceLink: <xsl:value-of select="@NodeName"/> 
<xsl:text> at: </xsl:text> <xsl:value-of select="@XPath"/> </H4>
<xsl:text>Message: </xsl:text> <xsl:value-of select="@Message"/>
<xsl:apply-templates/>
</xsl:template>


<!-- =============================================== -->

<xsl:template match="TestElement[@ErrorType='InvalidResourceLink']">
<H4>Invalid ResourceLink: <xsl:value-of select="@NodeName"/>
<xsl:text> at: </xsl:text> <xsl:value-of select="@XPath"/> </H4>
<xsl:text>Message: </xsl:text> <xsl:value-of select="@Message"/>
<xsl:apply-templates/>
</xsl:template>

<!-- =============================================== -->

<xsl:template match="TestElement[@ErrorType='ResLinkMultipleID']">
<H4>ResourceLink <xsl:value-of select="@NodeName"/> refers to multiply defined ID: 
at: <xsl:value-of select="@XPath"/> </H4>
Message: <xsl:value-of select="@Message"/>
<xsl:apply-templates/>
</xsl:template>

<!-- =============================================== -->

<xsl:template match="TestElement[@ErrorType='DanglingResLink']">
<H4>Dangling ResourceLink <xsl:value-of select="@NodeName"/>
 at: <xsl:value-of select="@XPath"/> </H4>
Message: <xsl:value-of select="@Message"/>
<xsl:apply-templates/>
</xsl:template>

<!-- =============================================== -->

<xsl:template match="TestElement[@ErrorType='DanglingPartResLink']">
<H4>Dangling Partitioned ResourceLink <xsl:value-of select="@NodeName"/>
 at: <xsl:value-of select="@XPath"/> </H4>
Message: <xsl:value-of select="@Message"/>
<br/>Resource PartUsage: <xsl:value-of select="@ResourcePartUsage"/>
<xsl:apply-templates/>
</xsl:template>

<!-- =============================================== -->

<xsl:template match="TestElement[@ErrorType='DanglingRefElement']">
<H4>Dangling RefElement <xsl:value-of select="@NodeName"/>
 at: <xsl:value-of select="@XPath"/> </H4>
Message: <xsl:value-of select="@Message"/>
<xsl:apply-templates/>
</xsl:template>
<!-- =============================================== -->

<xsl:template match="TestElement[@ErrorType='DanglingPartRefElement']">
<H4>Dangling Partitioned  RefElement <xsl:value-of select="@NodeName"/>
 at: <xsl:value-of select="@XPath"/> </H4>
Message: <xsl:value-of select="@Message"/>
<br/>Resource PartUsage: <xsl:value-of select="@ResourcePartUsage"/>
<xsl:apply-templates/>
</xsl:template>

<!-- =============================================== -->

<xsl:template match="TestElement[@ErrorType='InvalidPosition']">
<H4>Invalid position of Resource and ResourceLink <xsl:value-of select="@NodeName"/>
 at: <xsl:value-of select="@XPath"/> </H4>
Message: <xsl:value-of select="@Message"/>
<xsl:apply-templates/>
</xsl:template>

<!-- =============================================== -->

<xsl:template match="TestElement[@ErrorType='UnlinkedResource']">
<H4>Warning: Unlinked and Unreferenced Resource: <xsl:value-of select="@NodeName"/> 
<xsl:text> at: </xsl:text> <xsl:value-of select="@XPath"/></H4>
<xsl:apply-templates/>
</xsl:template>

<!-- =============================================== -->

<xsl:template match="TestElement[@ErrorType='InvalidRefElement']">
<H4>Invalid RefElement: <xsl:value-of select="@NodeName"/> 
<xsl:text> at: </xsl:text> <xsl:value-of select="@XPath"/></H4>
<xsl:text>Message: </xsl:text> <xsl:value-of select="@Message"/>
<xsl:apply-templates/>
</xsl:template>

<!-- =============================================== -->
<xsl:template match="TestElement[@ErrorType='PrivateContents']">
<H4>Invalid RefElement: <xsl:value-of select="@NodeName"/> 
<xsl:text> at: </xsl:text> <xsl:value-of select="@XPath"/></H4>
<xsl:text>Message: </xsl:text> <xsl:value-of select="@Message"/>
<xsl:apply-templates/>
</xsl:template>

<!-- =============================================== -->
<!-- display partition keys -->

<xsl:template match="TestElement/Part">
<xsl:for-each select="@*">
<br/>Partition key: <xsl:value-of select="name()"/> = <xsl:value-of select="."/>
</xsl:for-each>
</xsl:template>

<!-- =============================================== -->
<xsl:template match="DeviceCapTest"/>
<!-- =============================================== -->

<xsl:template match="TestElement">
<!-- =============================================== 
<H4>General Invalid Element: <xsl:value-of select="@NodeName"/> 
 at: <xsl:value-of select="@XPath"/></H4>
<xsl:text>Error Type: </xsl:text> <xsl:value-of select="@ErrorType"/>
<br/>
<xsl:text>Message: </xsl:text> <xsl:value-of select="@Message"/>
 =============================================== 
-->

<xsl:apply-templates/>
</xsl:template>

<!-- =============================================== -->
<xsl:template match="TestFile/Error">
<H3><font color="#ff0000">General Error:</font></H3>
<xsl:text>Error Type: </xsl:text> <xsl:value-of select="@ErrorType"/>
<br/>
<xsl:text>Message: </xsl:text> <xsl:value-of select="@Message"/>
<xsl:apply-templates/>
</xsl:template>


<!-- =============================================== -->
<xsl:template match="SeparationPool">
<H3><font color="#000000">Warning: Inconsistent Separations:</font></H3>
<xsl:apply-templates/>
</xsl:template>

<!-- =============================================== -->
<xsl:template match="SeparationPool/Warning[@ErrorType='MissingSeparation']">
<H4>Separation not defined in ColorPool: <xsl:value-of select="@Separation"/> </H4>
<xsl:apply-templates/>
</xsl:template>

<!-- =============================================== -->
<xsl:template match="SeparationPool/Warning[@ErrorType='UnreferencedSeparation']">
<H4>Separation defined in ColorPool but never referenced: <xsl:value-of select="@Separation"/> </H4>
<xsl:apply-templates/>
</xsl:template>

<!-- =============================================== -->
<!-- =============================================== -->
<!-- =============================================== -->

<!-- Attributes below here -->

<!-- =============================================== -->
<!-- =============================================== -->
<!-- =============================================== -->

 <xsl:template match="TestAttribute[@ErrorType='MultipleID']">
<H5>Multiple ID Attribute at: <xsl:value-of select="@XPath"/> </H5>
<br/>
<xsl:text>ID: </xsl:text>
<xsl:value-of select="@Value"/>
<br/>
<xsl:apply-templates/>
</xsl:template>

<!-- =============================================== -->

<xsl:template match="TestAttribute[@ErrorType='PreReleaseAttribute']">
<H5>Prerelease Attribute <xsl:value-of select="@NodeName"/>
 at: <xsl:value-of select="@XPath"/></H5>
First Valid Version: <xsl:value-of select="@FirstVersion"/>
Message:  <xsl:value-of select="@Message"/>
<br/>
<xsl:apply-templates/>
</xsl:template>
<!-- =============================================== -->

<xsl:template match="TestAttribute[@ErrorType='DeprecatedAttribute']">
<H5>Deprecated Attribute <xsl:value-of select="@NodeName"/>
 at: <xsl:value-of select="@XPath"/></H5>
Last Valid Version: <xsl:value-of select="@LastVersion"/><br/>
Message: <xsl:value-of select="@Message"/>
<br/>
<xsl:apply-templates/>
</xsl:template>

<!-- =============================================== -->

<xsl:template match="TestAttribute[@ErrorType='PrivateAttribute']">
<H5>Private Attribute <xsl:value-of select="@NodeName"/>
 at: <xsl:value-of select="@XPath"/></H5>
<xsl:text>Namespace Prefix: </xsl:text> <xsl:value-of select="@NSPrefix"/><BR/>
<xsl:text>Namespace URI: </xsl:text> <xsl:value-of select="@NSURI"/>
<xsl:apply-templates/>
</xsl:template>

<!-- =============================================== -->

<xsl:template match="TestAttribute[@ErrorType='UnknownAttribute']">
<H5>Unknown Attribute <xsl:value-of select="@NodeName"/>
 at: <xsl:value-of select="@XPath"/></H5>
<xsl:apply-templates/>
</xsl:template>

<!-- =============================================== -->

<xsl:template match="TestAttribute[@ErrorType='MissingAttribute']">
<H5>Missing Required Attribute <xsl:value-of select="@NodeName"/>
 at: <xsl:value-of select="@XPath"/></H5>
<xsl:apply-templates/>
</xsl:template>

<!-- =============================================== -->

<xsl:template match="TestAttribute[@ErrorType='InvalidAttribute']">
<H5>Invalid Attribute Value <xsl:value-of select="@NodeName"/>
 at: <xsl:value-of select="@XPath"/></H5>
Message: <xsl:value-of select="@Message"/>: <xsl:value-of select="@Value"/>
<xsl:apply-templates/>
</xsl:template>

<!-- =============================================== -->

<xsl:template match="TestAttribute[@ErrorType='SwapAttribute']">
<H5>Element written as Attribute: <xsl:value-of select="@NodeName"/>
 at: <xsl:value-of select="@XPath"/>; value: 
<xsl:value-of select="@Value"/></H5>
<xsl:apply-templates/>
</xsl:template>

<!-- =============================================== -->
<xsl:template match="TestAttribute">
<H5><font color="#ff0000">Tested Attribute <xsl:value-of select="@NodeName"/></font>
 at: <xsl:value-of select="@XPath"/></H5>
<xsl:text>Error Type: </xsl:text> <xsl:value-of select="@ErrorType"/>
<br/>
<xsl:text>Message: </xsl:text> <xsl:value-of select="@Message"/>
<br/>
<xsl:apply-templates/>
</xsl:template>

<!-- =============================================== -->
<!-- =============================================== -->
<!-- =============================================== -->
<!-- =============================================== -->


<xsl:template match="*">
<H4> <Font color="#ff3333">Unmatched element: TODO fix xslt for this</Font></H4>
<xsl:value-of select="name()"/>
<br/>
<xsl:apply-templates/>
</xsl:template>

<!-- =============================================== -->

</xsl:stylesheet>
