// Array to hold callback functions
var callbacks = []; 

// This function is called onload in the popup code
function getPageInfo(callback) { 
    // Add the callback to the queue
    callbacks.push(callback); 
    // Inject the content script into the current page 
    chrome.tabs.executeScript(null, { file: 'content_script.js' }); 
}; 

function sendEmailIfAvailable(callback) {
    callbacks.push(callback); 
    exists = emailExists();
    if (exists) {        
        alert('email does exist'); 
        chrome.tabs.executeScript(null, { file: 'content_script.js' });       
    } else {
        console.error('no email provided');
        alert('no email provided');
    }
}

// Perform the callback when a request is received from the content script
chrome.extension.onMessage.addListener(function(request)  { 
    // Get the first callback in the callbacks array
    // and remove it from the array
    alert('content_script result: ' + request);    
    sendEmail(decodeURIComponent(localStorage.email), request.title, encodeURIComponent(request.url));     
}); 

function emailExists(){
    alert('emailExists()'); 
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
    chrome.extension.getBackgroundPage().getPageInfo(onPageInfo); 
};

function sendEmail(email, subject, body) {
    alert('sendEmail')
    $.ajax({ 
             type: "GET",
             dataType: "json",
             url: "https://script.google.com/macros/s/AKfycbwpNFmh9INcnlaLdW5WZxKVaWDaFMljAI6Mo4EJ_qYdW6XbRC2F/exec?email=" + email + "&subject=" + subject + "&body=" + body,
             success: function(data){        
                var callback = callbacks.shift();
                callback(request); 
                console.log(data);
                alert(data);
             },
             error: function(XMLHttpRequest, textStatus, errorThrown) {
                console.error(textStatus);
                console.error(errorThrown);
                alert(textStatus);
                alert(errorThrown);
            }
         });
};
