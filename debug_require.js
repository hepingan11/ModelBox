
try {
    console.log("Attempting to require 'vue/compiler-sfc'...");
    const sfc = require('vue/compiler-sfc');
    console.log("Success: vue/compiler-sfc found.", sfc ? "Object returned" : "Null returned");
} catch (e) {
    console.error("Failed to require 'vue/compiler-sfc':", e.message);
    console.error("Code:", e.code);
    try {
        console.log("Attempting to require '@vue/compiler-sfc'...");
        const sfc2 = require('@vue/compiler-sfc');
        console.log("Success: @vue/compiler-sfc found.");
    } catch (e2) {
        console.error("Failed to require '@vue/compiler-sfc':", e2.message);
    }
}
