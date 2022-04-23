<template>
  <div class="hello">
    <button @click="evalJs">eval js</button>
  </div>
</template>

<script>
export default {
  name: "HelloWorld",
  props: {
    msg: String,
  },
  methods: {
    evalJs: async function () {
      let res = await fetch("./dist/test.js");
      let js = await res.text();
      eval(js);
      console.log('eval js complete')
    },
  },
};
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
h3 {
  margin: 40px 0 0;
}
ul {
  list-style-type: none;
  padding: 0;
}
li {
  display: inline-block;
  margin: 0 10px;
}
a {
  color: #42b983;
}
</style>
