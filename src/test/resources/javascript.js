var EBOOK_PREVIOUS = "ebookPrevious";
var EBOOK_CURRENT = "ebookCurrent";


function isVisible(el) {
    var top = el.offsetTop;
    var left = el.offsetLeft;
    var width = el.offsetWidth;
    var height = el.offsetHeight;

    while(el.offsetParent) {
        el = el.offsetParent;
        top += el.offsetTop;
        left += el.offsetLeft;
    }

    return (
        top >= Math.round(window.pageYOffset) &&
        left >= Math.round(window.pageXOffset) &&
        (top + height) <= Math.round(window.pageYOffset + window.innerHeight) &&
        (left + width) <= Math.round(window.pageXOffset + window.innerWidth)
    );
}

function partlyVisible(el) {
    var top = el.offsetTop;
    var left = el.offsetLeft;
    var width = el.offsetWidth;
    var height = el.offsetHeight;

    while(el.offsetParent) {
        el = el.offsetParent;
        top += el.offsetTop;
        left += el.offsetLeft;
    }

    return (
        top >= Math.round(window.pageYOffset) &&
        top  <= Math.round(window.pageYOffset + window.innerHeight) &&
        top+height >     Math.round(window.pageYOffset + window.innerHeight)
    );
}


function getTextNodes(textNodes,element){
    let nodes = element.childNodes;
    let i;
    for (i=0;i<nodes.length;i++) {
        let node = nodes[i];
        if (node.tagName=='P' || node.tagName=='H1'||node.tagName=='H2'|| node.tagName=='H3'|| node.tagName=='IMG') {
            textNodes.push(node);
        }
        else if (node.tagName=='DIV') {
            let result=getTextNodes([],node);
            if (result.length==0) {
                textNodes.push(node);
            }
            else {
                for (let j=0;j<result.length;j++) {
                    textNodes.push(result[j]);
                }
            }
        }
        else {
            textNodes = getTextNodes(textNodes,node);
        }
    }
    return textNodes;
}

function next() {
    return customNext();
}
function prev() {
    return customPrev();
}

function nextPage() {
    window.scroll(0,window.pageYOffset+window.innerHeight);

}
function prevPage() {
    window.scroll(0,window.pageYOffset-window.innerHeight-2);
}

function addWordsReverse(node,offsetEnd) {
    let c = node.childNodes;
    for (let j=c.length-1;j>=0;j--){
        node.removeChild(c[j]);
    }
    let words = document.partialText.split(" ");
    let j;
    let lastNode ;
    let total = true;
    for (j=offsetEnd-1;j>=0;j--) {
        let w = words[j];

        if (j>0) {
            w=w+" ";
        }
        lastNode =document.createTextNode(w);
        node.insertBefore(lastNode,node.firstChild);
        if (!isVisible(node)) {
            total=false;
            node.removeChild(lastNode);
            break;
        }
    }

    return total;
}

function addWords(node,offset) {
    let c = node.childNodes;
    for (let j=c.length-1;j>=0;j--){
        node.removeChild(c[j]);
    }
    let words = document.partialText.split(" ");
    let j;
    let lastNode ;
    let total = true;
    for (j=offset;j<words.length;j++) {
        let w = words[j];

        if (j>0) {
            w=" "+w;
        }
        lastNode =document.createTextNode(w);
        node.appendChild(lastNode);
        if (!isVisible(node)) {
            total=false;
            break;
        }
    }
    if (total==false) {
        node.removeChild(lastNode);
        // no word could be visible we consider as not visible at all not partial
        if (j==0) {
            node.appendChild(document.createTextNode(document.partialText));
            document.partialText=null;
            node.style.display="none";
        }
        else {
            document.partialOffset = j;
            document.partialNode = node;
        }
    }
    else {
        document.partialText=null;
    }
    return total;
}

function enhanceDocument() {
    if (typeof document.partialText == "undefined") {
        Object.defineProperty(document,'partialText',{
            value: null,
            writable: true
        });
    }
    if (typeof document.partialOffset == "undefined") {
        Object.defineProperty(document,'partialOffset',{
            value: null,
            writable: true
        });
    }
    if (typeof document.partialNode == "undefined") {
        Object.defineProperty(document,'partialNode',{
            value: null,
            writable: true
        });
    }
    if (typeof document.nextTextNodes == "undefined") {
        Object.defineProperty(document,'nextTextNodes',{
            value: [],
            writable: true
        });
    }
    if (typeof document.previousTextNodes == "undefined") {
        Object.defineProperty(document,'previousTextNodes',{
            value: [],
            writable: true
        });
    }
    if (typeof document.currentPages == "undefined") {
        Object.defineProperty(document,'currentPages',{
            value: [],
            writable: true
        });
    }
}

function saveCurrentPageAndRemoveFromNext(toRemove) {
    let currentPage = {pages:[],
        partialText:null,
        partialOffset:null,
        partialNode:null
    };
    for (let i=0;i<toRemove.length;i++) {
        let node = toRemove[i];
        currentPage.pages.push(node);
        let index = document.nextTextNodes.indexOf(node);
        if (index!=-1) {
            document.nextTextNodes.splice(index,1);
        }
        else {
            //alert("node not found "+node.textContent);
        }
    }
    currentPage.partialOffset=document.partialOffset;
    currentPage.partialNode=document.partialNode;
    currentPage.partialText=document.partialText;
    document.currentPages.push(currentPage);
}

function customPrev() {
    enhanceDocument();
    if (document.currentPages.length==0) return next();
    let currentPage = document.currentPages[document.currentPages.length-1];
    // current page start with partial text
    if (currentPage.partialText!=null) {

        let previousSameNode = false;
        if (document.currentPages.length>1) {
            let previousPage = document.currentPages[document.currentPages.length-2];
            if (currentPage.partialNode===previousPage.partialNode) {
                previousSameNode = true ;
            }
        }
        if (previousSameNode===false) {
            // TODO : only remove if same node not in previous as partial node
            let c = currentPage.partialNode.childNodes;
            for (let j = c.length - 1; j >= 0; j--) {
                currentPage.partialNode.removeChild(c[j]);
            }
            currentPage.partialNode.appendChild(document.createTextNode(currentPage.partialText));
            document.nextTextNodes.unshift(currentPage.partialNode);
            currentPage.partialNode.style.display = "none";
        }
    }
    for (let j=currentPage.pages.length-1;j>=0;j--) {
        let node = currentPage.pages[j];
        node.style.display="none";
        document.nextTextNodes.unshift(node);
        document.previousTextNodes.splice(document.previousTextNodes.indexOf(node),1);
    }
    document.partialNode=null;
    document.partialOffset=null;
    document.partialText=null;

    document.currentPages.splice(document.currentPages.length-1,1);
    if (document.currentPages.length==0) return prev();
    currentPage = document.currentPages[document.currentPages.length-1];
    for (let j=0;j<currentPage.pages.length;j++) {
        let node = currentPage.pages[j];
        node.style.display="";
    }

    if (currentPage.partialText!=null) {
        document.partialOffset = currentPage.partialOffset;
        document.partialText = currentPage.partialText;
        document.partialNode = currentPage.partialNode;
        document.partialNode.style.display="";
        addWordsReverse(currentPage.partialNode,document.partialOffset);
    }
    currentPage.pages[0].scrollIntoView(true);
}

function customNext() {
    enhanceDocument();
    if (document.nextTextNodes.length==0) return;
    let firstNode = true;
    for (let i=0;i<document.previousTextNodes.length;i++) {
        let node = document.previousTextNodes[i];
        node.style.display="none";
    }
    let toRemove=[];
    if (document.partialText!==null) {
        firstNode = false;
        document.partialNode.scrollIntoView(true);
        let total=addWords(document.partialNode,document.partialOffset);

        if (total==false) {
            saveCurrentPageAndRemoveFromNext([]);
            return;
        }
        else {
            document.previousTextNodes.push(document.partialNode);
            toRemove.push(document.partialNode);
        }
    }
    document.partialText=null;
    document.partialOffset=0;
    document.partialNode=null;

    for (let i=0;i<document.nextTextNodes.length;i++) {
        let firstInPage = false ;
        let node =document.nextTextNodes[i];
        node.style.display="";
        if (firstNode==true) {
            firstInPage = true ;
            firstNode=false;
            node.scrollIntoView(true);
            node.scrollTo(0,0);
        }
        if ("IMG"===node.tagName) {
            if (!isVisible(node) && firstInPage==false) {
                node.style.display="none";
                break;
            }
            else {
                document.previousTextNodes.push(node);
                toRemove.push(node);
            }
        }
        else{
            if (partlyVisible(node)) {
                document.partialText = node.textContent;
                addWords(node,0);
                break;
            }
            else if (!isVisible(node)) {
                node.style.display="none";
                break;
            } else {
                document.previousTextNodes.push(node);
                toRemove.push(node);
            }
        }
    }
    saveCurrentPageAndRemoveFromNext(toRemove);
}

document.addEventListener('keydown', function (event) {


    var key = event.key || event.keyCode;
    if (key === 'ArrowLeft' || key === 37) {
        prev();
    }
    else if (key === 'ArrowRight' || key === 39) {
        next();
    }
},false);


function onLoad() {
    alert("version 3");
    document.body.style.overflow = "hidden";
    enhanceDocument();
    document.nextTextNodes = getTextNodes(document.nextTextNodes,document.body);

    for (let i=0;i<document.nextTextNodes.length;i++) {
        let node = document.nextTextNodes[i];
        let father = node.parentNode;
        /*Object.defineProperty(node,'previousNodeFather',{
            value: father,
            writable: true
        });*/
        node.style.display="none";
    }

    //document.body.style.textOverflow="ellipsis";
}

