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
        left >= window.pageXOffset
    );
}


function prev() {
    let body = document.getElementsByTagName('body')[0];
    let textNodes = [];
    textNodes = getTextNodes(textNodes,body);
    let firstVisible=null;

    for (i = 0; i < textNodes.length; i++) {
        var e = textNodes[i];
        if (!e.classList.contains(EBOOK_PREVIOUS)) {
            firstVisible =e ;
            break;
        }
    }
    if (firstVisible==null) return;
    firstVisible.classList.remove(EBOOK_CURRENT);
    let start = false;
    for (i = textNodes.length-1; i >=0 ; i--) {
        var e = textNodes[i];
        if (start==false) {
            if (firstVisible === e) start=true;
        }
        else {
            e.style.visibility = null;
            if (e.classList.contains(EBOOK_PREVIOUS)) e.classList.remove(EBOOK_PREVIOUS);
            e.scrollIntoView(true);
            if (!isVisible(firstVisible) ) {
                e.classList.add(EBOOK_CURRENT);
                break;
            }
        }

    }
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


function next_new() {
    window.scroll(0,window.pageYOffset+window.innerHeight);

}
function prev_new() {
    window.scroll(0,window.pageYOffset-window.innerHeight-2);
}
function next() {
    let body = document.getElementsByTagName('body')[0];
    let textNodes = [];
    let toHidde = [];
    textNodes = getTextNodes(textNodes,body);
    let i;
    let firstVisibleFound = false ;
    let found = false;
    for (i = 0; i < textNodes.length; i++) {
        let e = textNodes[i];
        if (e.classList.contains(EBOOK_PREVIOUS)) continue;
        if (isVisible(e)) {
            firstVisibleFound = true ;
            toHidde.push(e);
            e.classList.remove(EBOOK_CURRENT);
        }
        // go to next element not visible after visible content
        else {
            //same element as previously
            if (e.classList.contains(EBOOK_CURRENT)) {
                window.scrollBy(0, window.innerHeight);
            }
            else {
                e.classList.add(EBOOK_CURRENT);
                if (partlyVisible(e)) {
                    //window.scroll(0,)
                    window.scrollBy(0,window.innerHeight);
                }
                else {
                    e.scrollIntoView(true);
                }
                found=true;
            }
            break;
        }
    }

    // only hide if true
    if (found==true) {
        for (i=0;i<toHidde.length;i++) {
            let e = toHidde[i];
            //e.style.display = "none";
            //e.style.visibility = 'hidden';
            e.classList.add(EBOOK_PREVIOUS);
            //e.style.visibility = 'hidden';
        }
        return true;
    }

    return false;
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
    alert("version 6")
}