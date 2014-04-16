// This callback function is called when the content script has been 
// injected and returned its results
function onPageInfo(o)  { 
    console.log(o.title); 
    console.log(o.url); 
} 

// Global reference to the status display SPAN
var statusDisplay = null;

function emailExists(){
    email = localStorage.email;

    if(email == undefined || email == "") {
        return false;
    }
    return true;    
};

function saveEmail() {
    event.preventDefault();

    var email = encodeURIComponent(document.getElementById('email').value);    
    localStorage.email = email;
    window.close();
}

// When the popup HTML has loaded
window.addEventListener('load', function(evt) {
    // Handle the bookmark form submit event with our addBookmark function
    document.getElementById('addemail').addEventListener('submit', saveEmail);
    // Cache a reference to the status display SPAN
    statusDisplay = document.getElementById('status-display');
    // Call the getPageInfo function in the background page, injecting content_script.js 
    // into the current HTML page and passing in our onPageInfo function as the callback
    chrome.extension.getBackgroundPage().getPageInfo(onPageInfo);
    exists = emailExists();
    if (exists) {        
        window.close();
    }
});