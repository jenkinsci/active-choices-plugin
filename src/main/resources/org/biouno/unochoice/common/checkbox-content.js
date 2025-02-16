window.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll(".checkbox-content-data-holder").forEach((dataHolder) => {
        const { itemCount, maxCount, randomName } = dataHolder.dataset;

      if (itemCount > maxCount) {
        document.getElementById(`ecp_${randomName}`).style.height = "255px";
        document.getElementById(`ecp_${randomName}`).style.overflowY = "auto";
      }

    });
});
