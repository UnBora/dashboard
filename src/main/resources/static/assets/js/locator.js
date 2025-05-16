function mapInitialization(mapId, mapLocation, onMarkerDragend) {

    var lat = Number(defaultLatitude);
    var lng = Number(defaultLongitude);
    if (!isNaN(mapLocation.lat)) {
        lat = Number(mapLocation.lat);
        lng = Number(mapLocation.lng);
    }

    var map = new google.maps.Map(document.getElementById(mapId), {
        zoom: 15,
        center: {lat: lat, lng: lng},
        streetViewControlOptions: {
            position: google.maps.ControlPosition.LEFT_TOP
        }
    });
    var marker = new google.maps.Marker({
        map: map,
        draggable: true,
        animation: google.maps.Animation.DROP,
        position: {lat: lat, lng: lng}
    });
    marker.addListener('click', function () {
        if (marker.getAnimation() !== null) {
            marker.setAnimation(null);
        } else {
            marker.setAnimation(google.maps.Animation.BOUNCE);
        }
    });

    google.maps.event.addListener(marker, "dragend", function (event) {
        var selectedLat = event.latLng.lat();
        var selectedLng = event.latLng.lng();
        onMarkerDragend(selectedLat, selectedLng);
    });
}