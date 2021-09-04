const listContainer = document.querySelector('#service-list');
let servicesRequest = new Request('/service');
fetch(servicesRequest)
.then(function(response) { return response.json(); })
.then(function(serviceList) {
    var tr = document.createElement('tr');
    var th1 = document.createElement('th');
    var th2 = document.createElement('th');
    var th3 = document.createElement('th');
    var th4 = document.createElement('th');
    var th5 = document.createElement('th');

    th1.appendChild(document.createTextNode('Name'));
    tr.appendChild(th1);
    th2.appendChild(document.createTextNode('Link'));
    tr.appendChild(th2);
    th3.appendChild(document.createTextNode('Status'));
    tr.appendChild(th3);
    th4.appendChild(document.createTextNode('Date'));
    tr.appendChild(th4);
    th5.appendChild(document.createTextNode(''));
    tr.appendChild(th5);
    listContainer.appendChild(tr);

  serviceList.forEach(service => {
      var tr = document.createElement('tr');
      var a = document.createElement("a");

      var name = document.createElement('td'); // Service Name
      var link = document.createElement('td'); // Service Link
      var status = document.createElement('td'); // Service Status
      var date = document.createElement('td'); // Service Creation Date
      var del = document.createElement('td'); // Service Deletion

      // Add hyperlink & status for each service
      a.textContent = service.url;
      a.setAttribute('href', service.url);

      // Add delete icon for each service
      var delIcon = document.createElement("i");
      delIcon.setAttribute('class', "fas fa-trash-alt");
      delIcon.setAttribute("id","delete-service");
      delIcon.onclick = function() {
          deleteUrl(service.url, service.creationDate, service.name);
        };

      var editIcon = document.createElement("i");
      editIcon.setAttribute('class', "fas fa-edit");
      editIcon.setAttribute("id","update-service");
      editIcon.onclick = function() {
        console.log(service.url);
        newNamePopup(service.url)
       };

      name.appendChild(document.createTextNode(service.name));
      link.appendChild(a);
      status.appendChild(document.createTextNode(service.status));
      date.appendChild(document.createTextNode(service.creationDate));
      del.appendChild(delIcon);
      del.appendChild(editIcon);
      delIcon.style.marginRight = "5px";
      delIcon.style.fontSize = "16px";
      editIcon.style.fontSize = "16px";
      tr.appendChild(name);
      setStyle('220px', name, tr);
      setStyle('150px', link, tr);
      setStyle('80px', status, tr);
      setStyle('200px', date, tr);
      setStyle('50px', del, tr);
      listContainer.appendChild(tr);
  });
});

const saveButton = document.querySelector('#post-service');
saveButton.onclick = evt => {
    let urlName = document.querySelector('#url-name').value;
    fetch('/service', {
        method: 'post',
        headers: {
        'Accept': 'application/json, text/plain, */*',
        'Content-Type': 'application/json'
        },
      body: JSON.stringify({url:urlName})
    }).then(res=> location.reload());
}

function deleteUrl(urlName, creationDate, name) {
    fetch('/service', {
        method: 'delete',
        headers: {
        'Accept': 'application/json, text/plain, */*',
        'Content-Type': 'application/json'
        },
      body: JSON.stringify({url:urlName, date: creationDate, name: name})
    }).then(res=> location.reload());
}

function newNamePopup(url) {
    var newName = prompt("Please enter the service's new name:", "");
    if (newName != null && newName != "") {
      updateName(url, newName);
    }
}

function updateName(urlName, name) {
    fetch('/service', {
        method: 'put',
        headers: {
        'Accept': 'application/json, text/plain, */*',
        'Content-Type': 'application/json'
        },
      body: JSON.stringify({url:urlName, name: name})
    }).then(res=> location.reload());
}

function setStyle(width, elem, tr) {
    tr.appendChild(elem);
    elem.style.width = width;
    elem.style.textAlign = 'center';
}