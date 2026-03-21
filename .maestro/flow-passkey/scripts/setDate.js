const today = new Date();

// Current date parts
const day = String(today.getDate()).padStart(2, '0');
const month = String(today.getMonth() + 1).padStart(2, '0');
const year = today.getFullYear();
const currentDate = `${day}/${month}/${year}`;

// Past years
const lastYear = year - 1;
const pastYearC = year - 10; // Child
const pastYearP = year - 30; // Parent

// Yesterday
const yesterdayObj = new Date(today);
yesterdayObj.setDate(today.getDate() - 1);
const yesterdayDay = String(yesterdayObj.getDate()).padStart(2, '0');
const yesterdayMonth = String(yesterdayObj.getMonth() + 1).padStart(2, '0');
const yesterdayYear = yesterdayObj.getFullYear();
const yesterdayDate = `${yesterdayDay}/${yesterdayMonth}/${yesterdayYear}`;

// Tomorrow
const tomorrowObj = new Date(today);
tomorrowObj.setDate(today.getDate() + 1);
const tomorrowDay = String(tomorrowObj.getDate()).padStart(2, '0');
const tomorrowMonth = String(tomorrowObj.getMonth() + 1).padStart(2, '0');
const tomorrowYear = tomorrowObj.getFullYear();
const tomorrowDate = `${tomorrowDay}/${tomorrowMonth}/${tomorrowYear}`;

// Past year dates
const lastYearDate = `${day}/${month}/${lastYear}`;
const pastYearDateC = `${day}/${month}/${pastYearC}`;
const pastYearDateP = `${day}/${month}/${pastYearP}`;

// Time calculations
const hours = String(today.getHours()).padStart(2, '0');
const minutes = String(today.getMinutes()).padStart(2, '0');
const minutesPlus3 = String(today.getMinutes() + 3).padStart(2, '0');
const hoursMinus1 = String(today.getHours() - 1).padStart(2, '0');
const currentTime = `${hours}:${minutes}`;
const testTime = `${hours}:${minutesPlus3}`;
const delayTime = `${hoursMinus1}:${minutes}`;

// Output
output.yesterdayDate = yesterdayDate;
output.tomorrowDate = tomorrowDate;
output.delayTime = delayTime;
output.currentDate = currentDate;
output.currentTime = currentTime;
output.testTime = testTime;
output.lastYearDate = lastYearDate;
output.pastYearDateC = pastYearDateC;
output.pastYearDateP = pastYearDateP;
