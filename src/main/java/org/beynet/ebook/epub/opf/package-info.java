@XmlSchema(
        xmlns = {
                @XmlNs(prefix="dc", namespaceURI="http://purl.org/dc/elements/1.1/"),
                @XmlNs(prefix="opf", namespaceURI="http://www.idpf.org/2007/opf"),
        },
        namespace = "http://www.idpf.org/2007/opf",
        elementFormDefault = XmlNsForm.QUALIFIED,
        attributeFormDefault = XmlNsForm.UNQUALIFIED)
package org.beynet.ebook.epub.opf;

import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchema;