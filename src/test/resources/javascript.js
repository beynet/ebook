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
        top >= window.pageYOffset &&
        left >= window.pageXOffset &&
        (top + height) <= (window.pageYOffset + window.innerHeight) &&
        (left + width) <= (window.pageXOffset + window.innerWidth)
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
        top >= window.pageYOffset &&
        top  <= (window.pageYOffset + window.innerHeight) &&
        top+height >     (window.pageYOffset + window.innerHeight)
    );
}


function getTextNodes(textNodes,element){
    let nodes = element.childNodes;
    let i;
    for (i=0;i<nodes.length;i++) {
        let node = nodes[i];
        if (node.tagName=='P' || node.tagName=='H1'||node.tagName=='H2'|| node.tagName=='H3') {
            textNodes.push(node);
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
    let words = document.currentPartial.split(" ");
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
            break;
        }
    }
    if (total==false) {
        node.removeChild(lastNode);
        document.partialOffset=j;
        document.partialNode=node;
    }
    else {
        document.currentPartial=null;
    }
    return total;
}

function addWords(node,offset) {
    let c = node.childNodes;
    for (let j=c.length-1;j>=0;j--){
        node.removeChild(c[j]);
    }
    let words = document.currentPartial.split(" ");
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
        document.partialOffset=j;
        document.partialNode=node;
    }
    else {
        document.currentPartial=null;
    }
    return total;
}

function enhanceDocument() {
    if (typeof document.currentPartial == "undefined") {
        Object.defineProperty(document,'currentPartial',{
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

function customPrev() {
    enhanceDocument();
    let currentPage = document.currentPages[document.currentPages.length-1];
    for (let j=currentPage.pages.length-1;j>=0;j--) {
        let node = currentPage.pages[j];
        node.style.display="none";
        document.nextTextNodes.unshift(node);
        document.previousTextNodes.splice(document.previousTextNodes.indexOf(node),1);
    }
    document.currentPages.splice(document.currentPages.length-1,1);
    if (document.currentPages.length==0) return prev();
    currentPage = document.currentPages[document.currentPages.length-1];
    for (let j=currentPage.pages.length-1;j>=0;j--) {
        let node = currentPage.pages[j];
        node.style.display="";
        document.nextTextNodes.unshift(node);
    }

    if (currentPage.currentPartial!=null) {
        let previousOffset;
        document.currentPartial = currentPage.currentPartial;
        document.partialNode = currentPage.partialNode;
        addWordsReverse(currentPage.partialNode,previousOffset);
    }
}

function customNext() {
    enhanceDocument();

    for (let i=0;i<document.previousTextNodes.length;i++) {
        let node = document.previousTextNodes[i];
        node.style.display="none";
    }

    if (document.currentPartial!==null) {
        let total=addWords(document.partialNode,document.partialOffset);
        document.partialNode.scrollIntoView(true);
        if (total==false) return;
    }
    document.currentPartial=null;
    document.partialOffset=0;
    document.partialNode=null;
    let toRemove=[];
    for (let i=0;i<document.nextTextNodes.length;i++) {
        let node =document.nextTextNodes[i];
        node.style.display="";
        if (partlyVisible(node)) {
            alert("partial");
            document.currentPartial = node.textContent;
            let total=addWords(node,0);
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
    let currentPage = {pages:[],
                       currentPartial:null,
                       partialOffset:null,
                        partialNode:null
                      };
    for (let i=0;i<toRemove.length;i++) {
        let node = toRemove[i];
        currentPage.pages.push(node);
        document.nextTextNodes.splice(document.nextTextNodes.indexOf(node),1);
    }
    currentPage.partialOffset=document.partialOffset;
    currentPage.partialNode=document.partialNode;
    currentPage.currentPartial=document.currentPartial;
    document.currentPages.push(currentPage);
}

document.addEventListener('keydown', function (event) {


    var key = event.key || event.keyCode;
    if (key === 'ArrowLeft' || key === 37) {
        prev();
    }
    else if (key === 'ArrowRight' || key === 39) {
        next();
    }
},true);


function onLoad() {
    alert("version 9");
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

