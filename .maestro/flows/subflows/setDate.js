const today = new Date();
const day = today.getDate().toString().padStart(2, '0');
const month = (today.getMonth() + 1).toString().padStart(2, '0'); // Months are zero-indexed
const year = today.getFullYear();
const currentDate = `${day}/${month}/${year}`; // Format: DD/MM/YYYY
const lastYear = today.getFullYear() - 1;
const pastYearC = today.getFullYear() - 10;  // C for child
const pastYearP = today.getFullYear() - 30;  // P for Parent

const yesterday = (today.getDate() - 1).toString().padStart(2, '0');
const yesterdayDate = `${yesterday}/${month}/${year}`; // Format: DD/MM/YYYY
const lastYearDate = `${day}/${month}/${lastYear}`; // Same day and month, but last year
const pastYearDateC = `${day}/${month}/${pastYearC}`; // Same day and month, but 10 years ago
const pastYearDateP = `${day}/${month}/${pastYearP}`; // Same day and month, but 30 years ago

const tomorrow = (today.getDate() + 1).toString().padStart(2, '0');
const tomorrowDate = `${tomorrow}/${month}/${year}`; // Format: DD/MM/YYYY

const hours = String(today.getHours()).padStart(2, '0');
const minutes = String(today.getMinutes()).padStart(2, '0');
const minutesPlus3 = String(today.getMinutes() + 3).padStart(2, '0');
const hoursMinus1 = String(today.getHours() - 1).padStart(2, '0');
const currentTime = `${hours}:${minutes}`;
const testTime = `${hours}:${minutesPlus3}`;
const delayTime = `${hoursMinus1}:${minutes}`;

output.yesterdayDate = yesterdayDate;
output.tomorrowDate = tomorrowDate;
output.delayTime = delayTime;
output.currentDate = currentDate;
output.currentTime = currentTime;
output.testTime = testTime;
output.lastYearDate = lastYearDate; // Last year’s date
output.pastYearDateC = pastYearDateC;
output.pastYearDateP = pastYearDateP;