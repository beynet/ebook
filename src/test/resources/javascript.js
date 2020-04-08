var EBOOK_PREVIOUS = "ebookPrevious";
var EBOOK_CURRENT = "ebookCurrent";
var nextTextNodes=[];
var previousTextNodes=[];

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
    return firstNext();
}
function prev() {
    return prevPage();
}

function nextPage() {
    window.scroll(0,window.pageYOffset+window.innerHeight);

}
function prevPage() {
    window.scroll(0,window.pageYOffset-window.innerHeight-2);
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
}

function firstNext() {

    enhanceDocument();

    for (let i=0;i<previousTextNodes.length;i++) {
        let node = previousTextNodes[i];
        node.style.display="none";
    }

    if (document.currentPartial!==null) {
        let total=addWords(document.partialNode,document.partialOffset);
        if (total==false) return;
    }

    let toRemove=[];
    for (let i=0;i<nextTextNodes.length;i++) {
        let node =nextTextNodes[i];
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
        }
        previousTextNodes.push(node);
        toRemove.push(node);
    }
    for (let i=0;i<toRemove.length;i++) {
        let node = toRemove[i];
        nextTextNodes.splice(nextTextNodes.indexOf(node),1);
    }
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
    alert("version 8");
    nextTextNodes = getTextNodes(nextTextNodes,document.body);

    for (let i=0;i<nextTextNodes.length;i++) {
        let node = nextTextNodes[i];
        let father = node.parentNode;
        /*Object.defineProperty(node,'previousNodeFather',{
            value: father,
            writable: true
        });*/
        node.style.display="none";
    }

    //document.body.style.textOverflow="ellipsis";
}

