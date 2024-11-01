window.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll(".multi-select-data-holder").forEach((dataHolder) => {
        const { visibleItemCount, multiSelectId } = dataHolder.dataset;
        document.getElementById(multiSelectId).size = visibleItemCount;
    });
});
