window.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll(".radio-content-data-holder").forEach((dataHolder) => {
        const { itemCount, maxCount, randomName } = dataHolder.dataset;

        if (itemCount > maxCount) {
          document.getElementById(`ecp_${randomName}`).style.height = "255px";
          document.getElementById(`ecp_${randomName}`).style.overflowY = "auto";
        }
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
