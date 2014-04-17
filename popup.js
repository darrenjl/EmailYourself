// This callback function is called when the content script has been 
// injected and returned its results
function onPageInfo(o)  { 
    alert('onPageInfo()');   
} 

function onEmailResult(o) {
    alert('onEmailResult');   
}


function saveEmail() {
    alert('saveEmail()');   
}
// Global reference to the status display SPAN
var statusDisplay = null;


// When the popup HTML has loaded
window.addEventListener('load', function(evt) {
    //alert('Page loaded');
    // Handle the bookmark form submit event with our addBookmark function
    document.getElementById('addemail').addEventListener('submit', saveEmail);
    // Cache a reference to the status display SPAN
    statusDisplay = document.getElementById('status-display');
    // Call the getPageInfo function in the background page, injecting content_script.js 
    // into the current HTML page and passing in our onPageInfo function as the callback
    chrome.extension.getBackgroundPage().sendEmailIfAvailable(onEmailResult)        
});
