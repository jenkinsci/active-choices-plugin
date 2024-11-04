window.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll(".checkbox-content-data-holder").forEach((dataHolder) => {
        const { maxCount, randomName } = dataHolder.dataset;

        var height = 0;
        var refElement = document.getElementById(`ecp_${randomName}_0`);
        if (maxCount > 0 && refElement && refElement.offsetHeight != 0) {
            for (var i = 0; i < maxCount; i++) {
                height += refElement.offsetHeight + 3;
            }
        }
        else {
            height = maxCount * 25.5;
        }

        height = Math.floor(height);
        document.getElementById(`ecp_${randomName}`).style.height = height + "px";
    });
});
