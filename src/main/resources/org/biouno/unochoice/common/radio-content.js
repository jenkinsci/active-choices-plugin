window.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll(".radio-content-data-holder").forEach((dataHolder) => {
        const { getVisibleItemCount, visibleItemCount, randomName } = dataHolder.dataset;
        var height = 0;
        var maxCount = getVisibleItemCount;
        if (maxCount > visibleItemCount) {
            maxCount = visibleItemCount;
        }

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

    document.querySelectorAll(".radio-content-radio-input").forEach((radioInput) => {
        radioInput.addEventListener("change", (event) => {
            const target = event.target;
            const name = target.getAttribute("name");
            const id = target.getAttribute("otherid");

            UnoChoice.fakeSelectRadioButton(name, id);
        });
    });
});
